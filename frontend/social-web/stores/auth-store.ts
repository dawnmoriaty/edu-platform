import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { User } from '@/lib/types';

interface AuthState {
    accessToken: string | null;
    refreshToken: string | null;
    user: User | null;
    isAuthenticated: boolean;
}

interface AuthActions {
    setAuth: (accessToken: string, refreshToken?: string, user?: User) => void;
    setUser: (user: User) => void;
    logout: () => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>()(
    persist(
        (set) => ({
            // State
            accessToken: null,
            refreshToken: null,
            user: null,
            isAuthenticated: false,

            // Actions
            setAuth: (accessToken, refreshToken, user) =>
                set({
                    accessToken,
                    refreshToken: refreshToken ?? null,
                    user: user ?? null,
                    isAuthenticated: true,
                }),

            setUser: (user) => set({ user }),

            logout: () =>
                set({
                    accessToken: null,
                    refreshToken: null,
                    user: null,
                    isAuthenticated: false,
                }),
        }),
        {
            name: 'auth-storage',
            storage: createJSONStorage(() => localStorage),
            partialize: (state) => ({
                accessToken: state.accessToken,
                refreshToken: state.refreshToken,
                user: state.user,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);
