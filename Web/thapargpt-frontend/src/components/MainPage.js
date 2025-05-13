import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import styles from '../styles/MainPage.module.css';

const MainPage = () => {
  const [user, setUser] = useState(null);
  const [query, setQuery] = useState('');
  const [response, setResponse] = useState('');
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    const fetchData = async () => {
      try {
        // Fetch user data
        const userRes = await axios.get('/api/user', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUser(userRes.data);

        // Fetch history data
        const historyRes = await axios.get('/api/history', {
          headers: { Authorization: `Bearer ${token}` },
        });

        // Process history data
        const processedHistory = historyRes.data.queries
          .map((item) => ({
            query: item.query_text,
            response: item.response_text,
            created_at: item.created_at,
          }))
          .sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

        setHistory(processedHistory);
      } catch (err) {
        console.error('Data fetch error:', err);
        if (err.response?.status === 401) {
          localStorage.removeItem('token');
          navigate('/login');
        }
      }
    };

    fetchData();
  }, [navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setIsLoading(true);
    setError('');
    setResponse('');

    try {
      const token = localStorage.getItem('token');
      console.log('Making API request to /api/query with query:', query);
      const res = await axios.post(
        '/api/query',
        { query },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          validateStatus: (status) => status >= 200 && status < 600, // Handle all status codes
          timeout: 30000, // 30-second timeout
        }
      );

      console.log('API Response (Full):', res);
      console.log('API Response Data:', res.data);

      // Handle response based on status code
      if (res.status >= 200 && res.status < 300) {
        // Try different possible response fields
        let apiResponse = res.data.answer || res.data.response || res.data.result || res.data.message;

        // If the response is an object, try to extract a string
        if (apiResponse && typeof apiResponse === 'object') {
          apiResponse = apiResponse.text || apiResponse.message || JSON.stringify(apiResponse);
          console.log('Extracted string from object - apiResponse:', apiResponse);
        }

        // Validate the response
        if (!apiResponse || typeof apiResponse !== 'string') {
          console.error('Validation failed:', {
            apiResponse,
            type: typeof apiResponse,
            isString: typeof apiResponse === 'string',
          });
          throw new Error('Invalid response format from API');
        }

        // Update the response state
        setResponse(apiResponse);

        // Update history
        const newHistoryItem = {
          query,
          response: apiResponse,
          created_at: new Date().toISOString(),
        };

        setHistory((prev) => [newHistoryItem, ...prev]);
        setQuery('');
      } else {
        // Handle non-2xx status codes
        const errorMessage =
          res.data.response || res.data.error || res.data.message || `Server error (Status: ${res.status})`;
        setError(errorMessage);
      }
    } catch (err) {
      console.error('Query error:', err.message, err.code, err.config);
      if (err.code === 'ECONNABORTED') {
        setError('Request timed out. The server is taking too long to respond. Please try again later.');
      } else if (err.code === 'ERR_NETWORK') {
        setError('Network error. Please check your connection and try again.');
      } else if (err.response) {
        setError(
          err.response.data?.message || err.response.data?.error || `Server error (Status: ${err.response.status})`
        );
      } else {
        setError(err.message || 'Failed to process query');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const UserProfile = () => {
    navigate('/profile');
  };

  if (!user) {
    return <div className={styles.loading}>Loading user data...</div>;
  }

  return (
    <div className={styles.mainContainer}>
      <header className={styles.header}>
        <div className={styles.logo}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#fff" />
            <path d="M2 17L12 22L22 17" stroke="#fff" strokeWidth="2" />
            <path d="M2 12L12 17L22 12" stroke="#fff" strokeWidth="2" />
          </svg>
          ThaparGPT
        </div>
        <div className={styles.userActions}>
          <div className={styles.avatar} onClick={UserProfile} title={user.full_name}>
            {user.full_name?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <button className={styles.btn} onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <div className={styles.chatContainer}>
        <div className={styles.queryPanel}>
          <h3 className={styles.panelHeader}>Ask ThaparGPT</h3>
          <form onSubmit={handleSubmit}>
            <textarea
              className={styles.queryTextarea}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Type your question here..."
              required
              disabled={isLoading}
            />
            <button type="submit" className={styles.btn} disabled={isLoading || !query.trim()}>
              {isLoading ? (
                <>
                  <span className={styles.spinner}></span>
                  Processing...
                </>
              ) : (
                'Get Answer'
              )}
            </button>
          </form>
          {error && <div className={styles.errorMessage}>{error}</div>}
        </div>

        <div className={styles.responsePanel}>
          <h3 className={styles.panelHeader}>Response</h3>
          <div className={styles.responseContent}>
            {response ? (
              <div className={styles.responseText}>
                {response.split('\n').map((line, i) => (
                  <p key={i}>{line}</p>
                ))}
              </div>
            ) : (
              <p className={styles.textMuted}>
                {isLoading ? 'Generating response...' : 'Your response will appear here...'}
              </p>
            )}
          </div>
        </div>
      </div>

      <div className={styles.historySection}>
        <h3 className={styles.panelHeader}>Recent Queries</h3>
        {history.length > 0 ? (
          <ul className={styles.historyList}>
            {history.map((item, index) => (
              <li key={`${item.created_at}-${index}`} className={styles.historyItem}>
                <div className={styles.historyQuery}>
                  <strong>Q:</strong> {item.query}
                </div>
                <div className={styles.historyResponse}>
                  <strong>A:</strong>
                  {item.response ? (
                    <div className={styles.responseText}>
                      {item.response.split('\n').map((line, i) => (
                        <p key={i}>{line}</p>
                      ))}
                    </div>
                  ) : (
                    <span className={styles.textMuted}>No response available</span>
                  )}
                </div>
                <div className={styles.historyDate}>
                  {new Date(item.created_at).toLocaleString()}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className={styles.textMuted}>No query history yet</p>
        )}
      </div>
    </div>
  );
};

export default MainPage;