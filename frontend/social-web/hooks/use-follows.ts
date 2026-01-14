'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as followsApi from '@/lib/api/follows';

// Query keys
export const followKeys = {
    all: ['follows'] as const,
    followers: (userId: string) => [...followKeys.all, 'followers', userId] as const,
    following: (userId: string) => [...followKeys.all, 'following', userId] as const,
    stats: (userId: string) => [...followKeys.all, 'stats', userId] as const,
    isFollowing: (userId: string) => [...followKeys.all, 'isFollowing', userId] as const,
};

// Get followers
export function useFollowers(userId: string, page = 1, size = 20) {
    return useQuery({
        queryKey: followKeys.followers(userId),
        queryFn: () => followsApi.getFollowers(userId, page, size),
        enabled: !!userId,
    });
}

// Get following
export function useFollowing(userId: string, page = 1, size = 20) {
    return useQuery({
        queryKey: followKeys.following(userId),
        queryFn: () => followsApi.getFollowing(userId, page, size),
        enabled: !!userId,
    });
}

// Get follow stats
export function useFollowStats(userId: string) {
    return useQuery({
        queryKey: followKeys.stats(userId),
        queryFn: () => followsApi.getFollowStats(userId),
        enabled: !!userId,
    });
}

// Check if following
export function useIsFollowing(userId: string) {
    return useQuery({
        queryKey: followKeys.isFollowing(userId),
        queryFn: () => followsApi.isFollowing(userId),
        enabled: !!userId,
    });
}

// Follow mutation
export function useFollow() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (userId: string) => followsApi.followUser(userId),
        onSuccess: (_, userId) => {
            queryClient.invalidateQueries({ queryKey: followKeys.stats(userId) });
            queryClient.invalidateQueries({ queryKey: followKeys.isFollowing(userId) });
            queryClient.invalidateQueries({ queryKey: followKeys.following('') });
        },
    });
}

// Unfollow mutation
export function useUnfollow() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (userId: string) => followsApi.unfollowUser(userId),
        onSuccess: (_, userId) => {
            queryClient.invalidateQueries({ queryKey: followKeys.stats(userId) });
            queryClient.invalidateQueries({ queryKey: followKeys.isFollowing(userId) });
            queryClient.invalidateQueries({ queryKey: followKeys.following('') });
        },
    });
}

// Combined toggle hook
export function useToggleFollow() {
    const followMutation = useFollow();
    const unfollowMutation = useUnfollow();

    return {
        toggle: (userId: string, isFollowing: boolean) => {
            if (isFollowing) {
                unfollowMutation.mutate(userId);
            } else {
                followMutation.mutate(userId);
            }
        },
        isLoading: followMutation.isPending || unfollowMutation.isPending,
    };
}
