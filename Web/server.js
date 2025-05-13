require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Pool } = require('pg');
const cors = require('cors');
const axios = require('axios');

const app = express();
const port = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'secretkey';
const NGROK_API = process.env.REACT_APP_BACKEND_URL || 'https://842a-106-219-122-87.ngrok-free.app/api/ask';

app.use(cors());
app.use(bodyParser.json());

app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`, {
        body: req.body,
        headers: req.headers,
        ip: req.ip
    });
    next();
});

const pool = new Pool({
    user: process.env.DB_USER || 'postgres',
    host: process.env.DB_HOST || 'localhost',
    database: process.env.DB_NAME || 'thapargpt',
    password: process.env.DB_PASSWORD || 'Password@158',
    port: process.env.DB_PORT || 5432,
    max: 20,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 2000,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false,
    application_name: 'thapargpt-backend'
});

pool.on('error', (err) => {
    console.error('Unexpected database error:', err);
});

const authenticate = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ message: 'Authorization header missing' });

    const token = authHeader.split(' ')[1];
    if (!token) return res.status(401).json({ message: 'Bearer token missing' });

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        const user = await pool.query('SELECT id FROM users WHERE id = $1', [decoded.id]);
        if (user.rows.length === 0) return res.status(401).json({ message: 'User not found' });

        req.user = decoded;
        next();
    } catch (err) {
        console.error('Authentication error:', err);
        return res.status(401).json({ message: err.name === 'TokenExpiredError' ? 'Token expired' : 'Invalid token' });
    }
};

function generateToken(user) {
    return jwt.sign({ id: user.id, username: user.username, email: user.email }, JWT_SECRET, { expiresIn: '24h' });
}

// SIGNUP
app.post('/api/signup', async (req, res) => {
    const { username, email, password, full_name, thapar_id } = req.body;
    if (!username || !email || !password) {
        return res.status(400).json({ error: 'Username, email, and password are required' });
    }

    try {
        const existing = await pool.query('SELECT * FROM users WHERE username = $1 OR email = $2', [username, email]);
        if (existing.rows.length > 0) return res.status(400).json({ error: 'Username or email already exists' });

        const passwordHash = await bcrypt.hash(password, await bcrypt.genSalt(10));
        const result = await pool.query(
            'INSERT INTO users (username, email, password_hash, full_name, thapar_id) VALUES ($1, $2, $3, $4, $5) RETURNING *',
            [username, email, passwordHash, full_name, thapar_id]
        );
        const token = generateToken(result.rows[0]);

        res.status(201).json({
            message: 'User created successfully',
            user: {
                id: result.rows[0].id,
                username: result.rows[0].username,
                email: result.rows[0].email,
                full_name: result.rows[0].full_name,
                thapar_id: result.rows[0].thapar_id
            },
            token
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Server error during signup' });
    }
});

// LOGIN
app.post('/api/login', async (req, res) => {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ message: 'Email and password are required' });

    try {
        const result = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
        if (result.rows.length === 0 || !(await bcrypt.compare(password, result.rows[0].password_hash))) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const user = result.rows[0];
        const token = generateToken(user);

        const client = await pool.connect();
        try {
            await client.query('BEGIN');
            await client.query('UPDATE user_sessions SET expires_at = NOW() WHERE user_id = $1', [user.id]);
            await client.query(
                `INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, expires_at)
                 VALUES ($1, $2, $3, $4, NOW() + interval '24 hours')`,
                [user.id, token, req.ip, req.headers['user-agent'] || '']
            );
            await client.query('COMMIT');
            res.json({ token, user });
        } catch (err) {
            await client.query('ROLLBACK');
            throw err;
        } finally {
            client.release();
        }
    } catch (err) {
        console.error('Login error:', err);
        res.status(500).json({ message: 'Server error during authentication' });
    }
});

// QUERY
app.post('/api/query', authenticate, async (req, res) => {
    const { query } = req.body;
    if (!query || typeof query !== 'string') {
        return res.status(400).json({ success: false, message: 'Valid query text is required' });
    }

    const client = await pool.connect();
    let queryId = null;
    try {
        await client.query('BEGIN');

        const insertRes = await client.query(
            'INSERT INTO query_history (user_id, query_text, status) VALUES ($1, $2, $3) RETURNING id',
            [req.user.id, query, 'pending']
        );
        queryId = insertRes.rows[0].id;

        const ragRes = await axios.post(NGROK_API, {
  query,
  user_id: req.user.id,
  query_id: queryId
}, {
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000
});

console.log("NGROK API Response:", ragRes.data);


        const answer = ragRes.data.answer || ragRes.data;

        await client.query(
            'UPDATE query_history SET response_text = $1, status = $2, metadata = $3 WHERE id = $4',
            [JSON.stringify(answer), 'completed', JSON.stringify({ processed_at: new Date() }), queryId]
        );

        await client.query('COMMIT');
        res.json({ success: true, query_id: queryId, answer });
    } catch (err) {
    await client.query('ROLLBACK');

    if (queryId) {
        await pool.query(
            'UPDATE query_history SET status = $1, is_error = TRUE, error_message = $2 WHERE id = $3',
            ['failed', err.message, queryId]
        );
    }

    // Axios-specific error handling
    if (err.response) {
        console.error('RAG API responded with error:', err.response.status, err.response.data);
    } else if (err.request) {
        console.error('No response received from RAG API:', err.request);
    } else {
        console.error('Error while making request to RAG API:', err.message);
    }

    res.status(500).json({ success: false, message: 'Query processing failed', error: err.message });
}
 finally {
        client.release();
    }
});

// GET USER
app.get('/api/user', authenticate, async (req, res) => {
    try {
        const result = await pool.query(
            `SELECT id, username, email, full_name, thapar_id, created_at,
                    (SELECT COUNT(*) FROM query_history WHERE user_id = $1) as query_count,
                    (SELECT MAX(created_at) FROM query_history WHERE user_id = $1) as last_query_time
             FROM users WHERE id = $1`, [req.user.id]
        );
        if (result.rows.length === 0) return res.status(404).json({ message: 'User not found' });
        res.json(result.rows[0]);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch user profile' });
    }
});

// GET QUERY HISTORY
app.get('/api/history', authenticate, async (req, res) => {
    const { limit = 50, offset = 0 } = req.query;
    try {
        const history = await pool.query(
            `SELECT id, query_text, response_text, created_at, is_error 
             FROM query_history WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3`,
            [req.user.id, parseInt(limit), parseInt(offset)]
        );
        const count = await pool.query('SELECT COUNT(*) FROM query_history WHERE user_id = $1', [req.user.id]);
        res.json({ queries: history.rows, total_count: parseInt(count.rows[0].count), limit, offset });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to fetch query history' });
    }
});

// LOGOUT
app.post('/api/logout', authenticate, async (req, res) => {
    try {
        await pool.query('UPDATE user_sessions SET expires_at = NOW() WHERE session_token = $1', [req.headers.authorization.split(' ')[1]]);
        res.json({ message: 'Successfully logged out' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Failed to logout' });
    }
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({ message: 'Internal server error' });
});

// Database setup
async function initializeDatabase() {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        await client.query(`
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name VARCHAR(100),
                thapar_id VARCHAR(20) UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);

        await client.query(`
            CREATE TABLE IF NOT EXISTS user_sessions (
                id SERIAL PRIMARY KEY,
                user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                session_token TEXT NOT NULL UNIQUE,
                ip_address TEXT,
                user_agent TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL
            )
        `);

        await client.query(`
            CREATE TABLE IF NOT EXISTS query_history (
                id SERIAL PRIMARY KEY,
                user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                query_text TEXT NOT NULL,
                response_text TEXT,
                metadata JSONB,
                is_error BOOLEAN DEFAULT FALSE,
                error_message TEXT,
                status TEXT DEFAULT 'pending',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);

        await client.query('COMMIT');
        console.log('✅ Database initialized successfully');
    } catch (err) {
        await client.query('ROLLBACK');
        console.error('❌ Database initialization failed:', err);
    } finally {
        client.release();
    }
}

initializeDatabase();
app.listen(port, () => console.log(`🚀 Server is running on port ${port}`));
