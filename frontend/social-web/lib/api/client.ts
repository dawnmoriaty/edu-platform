// API Client configuration with axios
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/auth-store';

// Environment-based API URLs
const SOCIAL_API_URL = process.env.NEXT_PUBLIC_SOCIAL_API_URL || 'http://localhost:8001/api/v1';
const AUTH_API_URL = process.env.NEXT_PUBLIC_AUTH_API_URL || 'http://localhost:8080/api/v1';

// Create axios instances
export const socialApi = axios.create({
  baseURL: SOCIAL_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

export const authApi = axios.create({
  baseURL: AUTH_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request interceptor - attach auth token
const attachAuthToken = (config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
};

socialApi.interceptors.request.use(attachAuthToken);
authApi.interceptors.request.use(attachAuthToken);

// Response interceptor - handle errors
const handleResponseError = async (error: AxiosError) => {
  if (error.response?.status === 401) {
    // Token expired - clear auth state
    useAuthStore.getState().logout();
    
    // Redirect to login if in browser
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  }
  return Promise.reject(error);
};

socialApi.interceptors.response.use((res) => res, handleResponseError);
authApi.interceptors.response.use((res) => res, handleResponseError);

// Generic API response type
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errors?: Record<string, string[]>;
}

// Pagination response type
export interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}
