// frontend/src/services/api.ts

import axios from 'axios';
import * as T from './types';

const API_BASE_URL = 'http://localhost/safereach/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Inject token into requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const api = {
  // Auth
  login: (data: any) => apiClient.post<T.ApiResponse<T.AuthResponse>>('/auth/login.php', data),
  register: (data: any) => apiClient.post<T.ApiResponse<any>>('/auth/register.php', data),

  // Requests
  createRequest: (data: any) => apiClient.post<T.ApiResponse<any>>('/requests/create.php', data),
  getActiveRequests: () => apiClient.get<T.ApiResponse<T.RequestHistoryItem[]>>('/requests/active.php'),
  getHistory: () => apiClient.get<T.ApiResponse<T.RequestHistoryItem[]>>('/requests/history.php'),
  trackRequest: (id: number) => apiClient.get<T.ApiResponse<any>>(`/requests/track.php?request_id=${id}`),
  submitFeedback: (data: any) => apiClient.post<T.ApiResponse<any>>('/requests/feedback.php', data),
  triggerSOS: (data: any) => apiClient.post<T.ApiResponse<any>>('/requests/sos.php', data),

  // Driver
  updateDriverStatus: (data: any) => apiClient.post<T.ApiResponse<any>>('/driver/status.php', data),
  updateLocation: (data: any) => apiClient.post<T.ApiResponse<any>>('/driver/location.php', data),

  // User
  getProfile: () => apiClient.get<T.ApiResponse<any>>('/user/profile.php'),
  getAddresses: () => apiClient.get<T.ApiResponse<any>>('/user/addresses.php'),
  getNotifications: () => apiClient.get<T.ApiResponse<any>>('/user/notifications.php'),

  // Hospitals
  getHospitals: () => apiClient.get<T.ApiResponse<T.Hospital[]>>('/hospitals/list.php'),

  // Analytics
  getAnalyticsSummary: () => apiClient.get<T.ApiResponse<T.AnalyticsSummary>>('/analytics/summary.php'),
};
