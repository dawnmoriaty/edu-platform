'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import * as likesApi from '@/lib/api/likes';
import { postKeys } from './use-posts';
import type { Post } from '@/lib/types';

// Query keys
export const likeKeys = {
    all: ['likes'] as const,
    count: (postId: string) => [...likeKeys.all, 'count', postId] as const,
    status: (postId: string) => [...likeKeys.all, 'status', postId] as const,
};

// Like post mutation with optimistic updates
export function useLikePost() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (postId: string) => likesApi.likePost(postId),
        // Optimistic update
        onMutate: async (postId) => {
            // Cancel outgoing queries
            await queryClient.cancelQueries({ queryKey: postKeys.detail(postId) });
            await queryClient.cancelQueries({ queryKey: postKeys.feed() });

            // Snapshot previous value
            const previousPost = queryClient.getQueryData<Post>(postKeys.detail(postId));

            // Optimistically update
            if (previousPost) {
                queryClient.setQueryData<Post>(postKeys.detail(postId), {
                    ...previousPost,
                    isLiked: true,
                    likeCount: previousPost.likeCount + 1,
                });
            }

            return { previousPost };
        },
        onError: (_, postId, context) => {
            // Rollback on error
            if (context?.previousPost) {
                queryClient.setQueryData(postKeys.detail(postId), context.previousPost);
            }
        },
        onSettled: (_, __, postId) => {
            // Always refetch to sync
            queryClient.invalidateQueries({ queryKey: postKeys.detail(postId) });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}

// Unlike post mutation with optimistic updates
export function useUnlikePost() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (postId: string) => likesApi.unlikePost(postId),
        // Optimistic update
        onMutate: async (postId) => {
            await queryClient.cancelQueries({ queryKey: postKeys.detail(postId) });
            await queryClient.cancelQueries({ queryKey: postKeys.feed() });

            const previousPost = queryClient.getQueryData<Post>(postKeys.detail(postId));

            if (previousPost) {
                queryClient.setQueryData<Post>(postKeys.detail(postId), {
                    ...previousPost,
                    isLiked: false,
                    likeCount: Math.max(0, previousPost.likeCount - 1),
                });
            }

            return { previousPost };
        },
        onError: (_, postId, context) => {
            if (context?.previousPost) {
                queryClient.setQueryData(postKeys.detail(postId), context.previousPost);
            }
        },
        onSettled: (_, __, postId) => {
            queryClient.invalidateQueries({ queryKey: postKeys.detail(postId) });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}

// Combined toggle hook
export function useToggleLike() {
    const likeMutation = useLikePost();
    const unlikeMutation = useUnlikePost();

    return {
        toggle: (postId: string, isLiked: boolean) => {
            if (isLiked) {
                unlikeMutation.mutate(postId);
            } else {
                likeMutation.mutate(postId);
            }
        },
        isLoading: likeMutation.isPending || unlikeMutation.isPending,
    };
}
