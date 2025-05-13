import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
// import './ProfilePage.css'; // We'll create this CSS file next

const ProfilePage = () => {
  const [userData, setUserData] = useState({
    username: '',
    email: '',
    full_name: '',
    thapar_id: '',
    created_at: '',
    query_count: 0,
    last_query_time: null
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({
    full_name: '',
    email: '',
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          navigate('/login');
          return;
        }

        const response = await axios.get('/api/user', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        setUserData(response.data);
        setFormData({
          full_name: response.data.full_name || '',
          email: response.data.email || '',
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to fetch user data');
        if (err.response?.status === 401) {
          localStorage.removeItem('token');
          navigate('/login');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [navigate]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.newPassword && formData.newPassword !== formData.confirmPassword) {
      setError('New passwords do not match');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const updateData = {
        full_name: formData.full_name,
        email: formData.email
      };

      if (formData.newPassword) {
        updateData.currentPassword = formData.currentPassword;
        updateData.newPassword = formData.newPassword;
      }

      const response = await axios.put('/api/user', updateData, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      setUserData(prev => ({
        ...prev,
        full_name: response.data.full_name,
        email: response.data.email
      }));

      setEditMode(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    }
  };

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('token');
      await axios.post('/api/logout', {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      localStorage.removeItem('token');
      navigate('/login');
    }
  };

  if (loading) {
    return (
      <div className="profile-container">
        <div className="loading-spinner"></div>
        <p>Loading profile data...</p>
      </div>
    );
  }

  if (error && !editMode) {
    return (
      <div className="profile-container">
        <div className="error-message">{error}</div>
        <button onClick={() => window.location.reload()} className="retry-button">
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="profile-container">
      <div className="profile-header">
        <h2>User Profile</h2>
        {!editMode && (
          <div className="profile-actions">
            <button onClick={() => setEditMode(true)} className="edit-button">
              Edit Profile
            </button>
            <button onClick={handleLogout} className="logout-button">
              Logout
            </button>
          </div>
        )}
      </div>

      {editMode ? (
        <form onSubmit={handleSubmit} className="profile-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label>Username</label>
            <input 
              type="text" 
              value={userData.username} 
              disabled 
              className="disabled-input"
            />
          </div>

          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              name="full_name"
              value={formData.full_name}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Thapar ID</label>
            <input 
              type="text" 
              value={userData.thapar_id} 
              disabled 
              className="disabled-input"
            />
          </div>

          <div className="password-section">
            <h3>Change Password</h3>

            <div className="form-group">
              <label>Current Password</label>
              <input
                type="password"
                name="currentPassword"
                value={formData.currentPassword}
                onChange={handleInputChange}
                placeholder="Leave blank to keep current password"
              />
            </div>

            <div className="form-group">
              <label>New Password</label>
              <input
                type="password"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-group">
              <label>Confirm New Password</label>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleInputChange}
              />
            </div>
          </div>

          <div className="form-actions">
            <button type="button" onClick={() => setEditMode(false)} className="cancel-button">
              Cancel
            </button>
            <button type="submit" className="save-button">
              Save Changes
            </button>
          </div>
        </form>
      ) : (
        <div className="profile-details">
          <div className="profile-avatar">
            {userData.username.charAt(0).toUpperCase()}
          </div>

          <div className="detail-row">
            <span className="detail-label">Username:</span>
            <span className="detail-value">{userData.username}</span>
          </div>

          <div className="detail-row">
            <span className="detail-label">Full Name:</span>
            <span className="detail-value">{userData.full_name || 'Not provided'}</span>
          </div>

          <div className="detail-row">
            <span className="detail-label">Email:</span>
            <span className="detail-value">{userData.email}</span>
          </div>

          <div className="detail-row">
            <span className="detail-label">Thapar ID:</span>
            <span className="detail-value">{userData.thapar_id}</span>
          </div>

          <div className="detail-row">
            <span className="detail-label">Member Since:</span>
            <span className="detail-value">
              {new Date(userData.created_at).toLocaleDateString()}
            </span>
          </div>

          <div className="stats-section">
            <div className="stat-card">
              <span className="stat-number">{userData.query_count}</span>
              <span className="stat-label">Queries Made</span>
            </div>

            <div className="stat-card">
              <span className="stat-number">
                {userData.last_query_time 
                  ? new Date(userData.last_query_time).toLocaleDateString() 
                  : 'Never'}
              </span>
              <span className="stat-label">Last Query</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;