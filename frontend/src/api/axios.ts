import axios from 'axios';

// Detect if we are on localhost or real IP
const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
const BASE_URL = isLocalhost
  ? 'http://localhost/safereach/api'
  : `http://${window.location.hostname}/safereach/api`;

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('safereach_token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('safereach_token');
      localStorage.removeItem('safereach_user');
      window.location.hash = '#login';
    }
    return Promise.reject(error);
  }
);

export default api;
