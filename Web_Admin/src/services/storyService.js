import axios from 'axios';

const API_URL = 'http://localhost:8080/story-speaker/stories';
const ADMIN_API_URL = 'http://localhost:8080/story-speaker/stories/admin';
const SEARCH_API_URL = 'http://localhost:8080/story-speaker/stories/admin/search';

const createStory = async (formData) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.post(API_URL, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const getStoryById = async (id) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.get(`${API_URL}/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const updateStory = async (id, formData) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.put(`${API_URL}/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const extractEpub = async (file) => {
  const token = localStorage.getItem('accessToken');
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await axios.post(`${API_URL}/extract-epub`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const saveExtractedStory = async (storyData) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.post(`${API_URL}/save-extracted`, storyData, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const getStoriesForAdmin = async (createdByRole, page = 0, size = 10) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.get(ADMIN_API_URL, {
      params: {
        createdByRole,
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

const searchStoriesForAdmin = async (keyword, createdByRole, page = 0, size = 10) => {
  const token = localStorage.getItem('accessToken');
  try {
    const response = await axios.post(SEARCH_API_URL, {
      keyword,
      createdByRole
    }, {
      params: {
        page,
        size
      },
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

const deleteStory = async (id) => {
  const token = localStorage.getItem('accessToken');
  try {
    await axios.delete(`${API_URL}/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  } catch (error) {
    throw error;
  }
};

const toggleWebView = async (id) => {
  const token = localStorage.getItem('accessToken');
  try {
    // PUT request with empty body
    await axios.put(`${API_URL}/${id}/toggle-webview`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  } catch (error) {
    throw error;
  }
};

const storyService = {
  createStory,
  getStoryById,
  updateStory,
  extractEpub,
  saveExtractedStory,
  getStoriesForAdmin,
  searchStoriesForAdmin,
  deleteStory,
  toggleWebView
};

export default storyService;
