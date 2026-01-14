import { socialApi, type ApiResponse } from './client';
import type { LikeResponse, LikeCountResponse, LikeStatusResponse } from '@/lib/types';

// Like a post
export async function likePost(postId: string): Promise<LikeResponse> {
    const response = await socialApi.post<ApiResponse<LikeResponse>>('/likes', { post_id: postId });
    return response.data.data;
}

// Unlike a post
export async function unlikePost(postId: string): Promise<void> {
    await socialApi.delete(`/posts/${postId}/likes`);
}

// Get like count for a post
export async function getLikeCount(postId: string): Promise<LikeCountResponse> {
    const response = await socialApi.get<ApiResponse<LikeCountResponse>>(
        `/posts/${postId}/likes/count`
    );
    return response.data.data;
}

// Get like status (is liked + count)
export async function getLikeStatus(postId: string): Promise<LikeStatusResponse> {
    const response = await socialApi.get<ApiResponse<LikeStatusResponse>>(
        `/posts/${postId}/likes/status`
    );
    return response.data.data;
}
