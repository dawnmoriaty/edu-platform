import { authApi, type ApiResponse } from './client';
import type {
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    User,
    RefreshTokenRequest
} from '@/lib/types';

// Login
export async function login(data: LoginRequest): Promise<AuthResponse> {
    const response = await authApi.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return response.data.data;
}

// Register
export async function register(data: Omit<RegisterRequest, 'confirmPassword'>): Promise<AuthResponse> {
    const response = await authApi.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return response.data.data;
}

// Get current user
export async function getCurrentUser(): Promise<User> {
    const response = await authApi.get<ApiResponse<User>>('/auth/me');
    return response.data.data;
}

// Refresh token
export async function refreshToken(data: RefreshTokenRequest): Promise<AuthResponse> {
    const response = await authApi.post<ApiResponse<AuthResponse>>('/auth/refresh', data);
    return response.data.data;
}

// Logout
export async function logout(): Promise<void> {
    await authApi.post('/auth/logout');
}
