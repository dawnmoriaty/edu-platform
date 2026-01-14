'use client';

import {
    useMutation,
    useQuery,
    useInfiniteQuery,
    useQueryClient
} from '@tanstack/react-query';
import * as postsApi from '@/lib/api/posts';
import type { CreatePostRequest, UpdatePostRequest, ListPostsParams } from '@/lib/types';

// Query keys
export const postKeys = {
    all: ['posts'] as const,
    lists: () => [...postKeys.all, 'list'] as const,
    list: (params: ListPostsParams) => [...postKeys.lists(), params] as const,
    feed: () => [...postKeys.all, 'feed'] as const,
    details: () => [...postKeys.all, 'detail'] as const,
    detail: (id: string) => [...postKeys.details(), id] as const,
};

// Get posts by user
export function usePosts(params: ListPostsParams) {
    return useQuery({
        queryKey: postKeys.list(params),
        queryFn: () => postsApi.getPosts(params),
        enabled: !!params.userId,
    });
}

// Get feed with infinite scroll
export function useFeed() {
    return useInfiniteQuery({
        queryKey: postKeys.feed(),
        queryFn: ({ pageParam = 1 }) => postsApi.getFeed(pageParam, 20),
        initialPageParam: 1,
        getNextPageParam: (lastPage) => {
            if (lastPage.page < lastPage.totalPages) {
                return lastPage.page + 1;
            }
            return undefined;
        },
    });
}

// Get single post
export function usePost(id: string) {
    return useQuery({
        queryKey: postKeys.detail(id),
        queryFn: () => postsApi.getPost(id),
        enabled: !!id,
    });
}

// Create post mutation
export function useCreatePost() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (data: CreatePostRequest) => postsApi.createPost(data),
        onSuccess: () => {
            // Invalidate feed and posts lists
            queryClient.invalidateQueries({ queryKey: postKeys.lists() });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}

// Update post mutation
export function useUpdatePost() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, data }: { id: string; data: UpdatePostRequest }) =>
            postsApi.updatePost(id, data),
        onSuccess: (_, { id }) => {
            queryClient.invalidateQueries({ queryKey: postKeys.detail(id) });
            queryClient.invalidateQueries({ queryKey: postKeys.lists() });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}

// Delete post mutation
export function useDeletePost() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => postsApi.deletePost(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: postKeys.lists() });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}
