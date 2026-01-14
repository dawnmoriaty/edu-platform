import { socialApi, type ApiResponse, type PageResponse } from './client';
import type { Comment, CreateCommentRequest, UpdateCommentRequest } from '@/lib/types';

// Get comments for a post
export async function getComments(
    postId: string,
    page = 1,
    size = 20
): Promise<PageResponse<Comment>> {
    const response = await socialApi.get<ApiResponse<PageResponse<Comment>>>(
        `/posts/${postId}/comments`,
        { params: { page, size } }
    );
    return response.data.data;
}

// Get single comment
export async function getComment(id: string): Promise<Comment> {
    const response = await socialApi.get<ApiResponse<Comment>>(`/comments/${id}`);
    return response.data.data;
}

// Create comment
export async function createComment(data: CreateCommentRequest): Promise<Comment> {
    const response = await socialApi.post<ApiResponse<Comment>>('/comments', data);
    return response.data.data;
}

// Update comment
export async function updateComment(id: string, data: UpdateCommentRequest): Promise<void> {
    await socialApi.put(`/comments/${id}`, data);
}

// Delete comment
export async function deleteComment(id: string): Promise<void> {
    await socialApi.delete(`/comments/${id}`);
}
