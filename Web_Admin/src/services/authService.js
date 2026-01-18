import axios from 'axios';

const API_URL = 'http://localhost:8080/story-speaker/auth';

const loginAdmin = async (username, password) => {
  try {
    const response = await axios.post(`${API_URL}/admin/login`, {
      username,
      password,
    });
    if (response.data.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      localStorage.setItem('userId', response.data.userId);
    }
    return response.data;
  } catch (error) {
    throw error;
  }
};

const logout = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  try {
    if (refreshToken) {
      await axios.post(`${API_URL}/logout`, {
        refreshToken
      });
    }
  } catch (error) {
    console.error('Logout failed on server:', error);
  } finally {
    // Always clear local storage even if server logout fails
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
  }
};

const authService = {
  loginAdmin,
  logout,
};

export default authService;
