import axios from 'axios';

const API_URL = 'http://localhost:8080/story-speaker/dashboard';

const getStats = async () => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.get(`${API_URL}/stats`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const dashboardService = {
  getStats,
};

export default dashboardService;
