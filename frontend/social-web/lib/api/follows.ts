import { socialApi, type ApiResponse, type PageResponse } from './client';
import type { FollowResponse, FollowStatsResponse, User } from '@/lib/types';

// Follow a user
export async function followUser(userId: string): Promise<FollowResponse> {
    const response = await socialApi.post<ApiResponse<FollowResponse>>('/follows', {
        user_id: userId
    });
    return response.data.data;
}

// Unfollow a user
export async function unfollowUser(userId: string): Promise<void> {
    await socialApi.delete(`/users/${userId}/follow`);
}

// Get followers of a user
export async function getFollowers(
    userId: string,
    page = 1,
    size = 20
): Promise<PageResponse<User>> {
    const response = await socialApi.get<ApiResponse<PageResponse<User>>>(
        `/users/${userId}/followers`,
        { params: { page, size } }
    );
    return response.data.data;
}

// Get users that a user is following
export async function getFollowing(
    userId: string,
    page = 1,
    size = 20
): Promise<PageResponse<User>> {
    const response = await socialApi.get<ApiResponse<PageResponse<User>>>(
        `/users/${userId}/following`,
        { params: { page, size } }
    );
    return response.data.data;
}

// Get follow stats for a user
export async function getFollowStats(userId: string): Promise<FollowStatsResponse> {
    const response = await socialApi.get<ApiResponse<FollowStatsResponse>>(
        `/users/${userId}/follow-stats`
    );
    return response.data.data;
}

// Check if current user is following a user
export async function isFollowing(userId: string): Promise<boolean> {
    const response = await socialApi.get<ApiResponse<{ is_following: boolean }>>(
        `/users/${userId}/is-following`
    );
    return response.data.data.is_following;
}
