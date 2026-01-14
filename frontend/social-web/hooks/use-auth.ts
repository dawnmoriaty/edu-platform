'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import * as authApi from '@/lib/api/auth';
import { useAuthStore } from '@/stores/auth-store';
import type { LoginRequest, RegisterRequest } from '@/lib/types';

// Query keys
export const authKeys = {
    all: ['auth'] as const,
    currentUser: () => [...authKeys.all, 'currentUser'] as const,
};

// Get current user
export function useCurrentUser() {
    const { isAuthenticated } = useAuthStore();

    return useQuery({
        queryKey: authKeys.currentUser(),
        queryFn: authApi.getCurrentUser,
        enabled: isAuthenticated,
        staleTime: 5 * 60 * 1000, // 5 minutes
        retry: false,
    });
}

// Login mutation
export function useLogin() {
    const queryClient = useQueryClient();
    const router = useRouter();
    const { setAuth } = useAuthStore();

    return useMutation({
        mutationFn: (data: LoginRequest) => authApi.login(data),
        onSuccess: (response) => {
            setAuth(response.accessToken, response.refreshToken, response.user);
            queryClient.invalidateQueries({ queryKey: authKeys.all });
            router.push('/');
        },
    });
}

// Register mutation
export function useRegister() {
    const router = useRouter();

    return useMutation({
        mutationFn: (data: Omit<RegisterRequest, 'confirmPassword'>) =>
            authApi.register(data),
        onSuccess: () => {
            router.push('/login?registered=true');
        },
    });
}

// Logout mutation
export function useLogout() {
    const queryClient = useQueryClient();
    const router = useRouter();
    const { logout } = useAuthStore();

    return useMutation({
        mutationFn: authApi.logout,
        onSuccess: () => {
            logout();
            queryClient.clear();
            router.push('/login');
        },
        onError: () => {
            // Even if API fails, clear local state
            logout();
            queryClient.clear();
            router.push('/login');
        },
    });
}
