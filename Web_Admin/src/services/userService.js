import axios from 'axios';

const API_URL = 'http://localhost:8080/story-speaker/users';

const getAllUsers = async (page = 0, size = 10) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.get(API_URL, {
      params: {
        page,
        size
      },
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const userService = {
  getAllUsers,
};

export default userService;
