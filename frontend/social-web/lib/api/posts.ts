import { socialApi, type ApiResponse, type PageResponse } from './client';
import type {
    Post,
    CreatePostRequest,
    UpdatePostRequest,
    ListPostsParams
} from '@/lib/types';

// Get posts by user
export async function getPosts(params: ListPostsParams): Promise<PageResponse<Post>> {
    const response = await socialApi.get<ApiResponse<PageResponse<Post>>>('/posts', { params });
    return response.data.data;
}

// Get single post
export async function getPost(id: string): Promise<Post> {
    const response = await socialApi.get<ApiResponse<Post>>(`/posts/${id}`);
    return response.data.data;
}

// Get feed (posts from followed users)
export async function getFeed(page = 1, size = 20): Promise<PageResponse<Post>> {
    const response = await socialApi.get<ApiResponse<PageResponse<Post>>>('/feed', {
        params: { page, size },
    });
    return response.data.data;
}

// Create post
export async function createPost(data: CreatePostRequest): Promise<Post> {
    const response = await socialApi.post<ApiResponse<Post>>('/posts', data);
    return response.data.data;
}

// Update post
export async function updatePost(id: string, data: UpdatePostRequest): Promise<void> {
    await socialApi.put(`/posts/${id}`, data);
}

// Delete post
export async function deletePost(id: string): Promise<void> {
    await socialApi.delete(`/posts/${id}`);
}
