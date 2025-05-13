import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

const Signup = () => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        fullName: '',
        thaparId: ''
    });
    const [errors, setErrors] = useState({
        username: '',
        email: '',
        password: '',
        form: ''
    });
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // Clear error when user types
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    const validateForm = () => {
        let valid = true;
        const newErrors = {
            username: '',
            email: '',
            password: '',
            form: ''
        };

        // Username validation
        if (!formData.username.trim()) {
            newErrors.username = 'Username is required';
            valid = false;
        } else if (formData.username.length < 3) {
            newErrors.username = 'Username must be at least 3 characters';
            valid = false;
        }

        // Email validation
        if (!formData.email.trim()) {
            newErrors.email = 'Email is required';
            valid = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Please enter a valid email address';
            valid = false;
        }

        // Password validation
        if (!formData.password) {
            newErrors.password = 'Password is required';
            valid = false;
        } else if (formData.password.length < 6) {
            newErrors.password = 'Password must be at least 6 characters';
            valid = false;
        }

        setErrors(newErrors);
        return valid;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setErrors({ ...errors, form: '' });

        if (!validateForm()) {
            setIsLoading(false);
            return;
        }

        try {
            const res = await axios.post('/api/signup', {
                username: formData.username,
                email: formData.email,
                password: formData.password,
                full_name: formData.fullName,
                thapar_id: formData.thaparId
            });

            if (res.data.success) {
                localStorage.setItem('token', res.data.token);
                navigate('/');
            } else {
                setErrors({
                    ...errors,
                    form: res.data.message || 'Signup failed. Please try again.',
                    ...(res.data.details || {})
                });
            }
        } catch (err) {
            let errorMessage = 'Signup failed. Please try again.';
            
            if (err.response) {
                // Server responded with error status
                const { data } = err.response;
                errorMessage = data.message || errorMessage;
                
                setErrors({
                    ...errors,
                    form: errorMessage,
                    ...(data.details || {})
                });
            } else if (err.request) {
                // Request was made but no response
                errorMessage = 'Network error. Please check your connection.';
                setErrors({ ...errors, form: errorMessage });
            } else {
                // Other errors
                console.error('Signup error:', err);
                setErrors({ ...errors, form: errorMessage });
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <h2>Create Account</h2>
            <p className="text-center mt-3">Join ThaparGPT today</p>
            
            {errors.form && (
                <div className="error-message">
                    {errors.form}
                    {Object.keys(errors).some(key => key !== 'form' && errors[key]) && (
                        <ul className="error-details">
                            {Object.entries(errors).map(([field, message]) => (
                                field !== 'form' && message && (
                                    <li key={field}>{message}</li>
                                )
                            ))}
                        </ul>
                    )}
                </div>
            )}
            
            <form onSubmit={handleSubmit} noValidate>
                <div className="form-group">
                    <label>Username</label>
                    <input
                        type="text"
                        name="username"
                        className={`form-control ${errors.username ? 'is-invalid' : ''}`}
                        value={formData.username}
                        onChange={handleChange}
                        required
                    />
                    {errors.username && (
                        <div className="invalid-feedback">{errors.username}</div>
                    )}
                </div>
                
                <div className="form-group">
                    <label>Email Address</label>
                    <input
                        type="email"
                        name="email"
                        className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />
                    {errors.email && (
                        <div className="invalid-feedback">{errors.email}</div>
                    )}
                </div>
                
                <div className="form-group">
                    <label>Password</label>
                    <input
                        type="password"
                        name="password"
                        className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                        value={formData.password}
                        onChange={handleChange}
                        required
                    />
                    {errors.password && (
                        <div className="invalid-feedback">{errors.password}</div>
                    )}
                </div>
                
                <div className="form-group">
                    <label>Full Name (Optional)</label>
                    <input
                        type="text"
                        name="fullName"
                        className="form-control"
                        value={formData.fullName}
                        onChange={handleChange}
                    />
                </div>
                
                <div className="form-group">
                    <label>Thapar ID (Optional)</label>
                    <input
                        type="text"
                        name="thaparId"
                        className="form-control"
                        value={formData.thaparId}
                        onChange={handleChange}
                    />
                </div>
                
                <button 
                    type="submit" 
                    className="btn btn-primary btn-block mt-3"
                    disabled={isLoading}
                >
                    {isLoading ? (
                        <>
                            <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                            <span className="ms-2">Creating account...</span>
                        </>
                    ) : 'Create Account'}
                </button>
            </form>
            
            <div className="text-center mt-3">
                <span>Already have an account? </span>
                <Link to="/login" className="link-text">Sign in</Link>
            </div>
        </div>
    );
};

export default Signup;