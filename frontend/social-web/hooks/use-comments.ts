'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as commentsApi from '@/lib/api/comments';
import { postKeys } from './use-posts';
import type { CreateCommentRequest, UpdateCommentRequest } from '@/lib/types';

// Query keys
export const commentKeys = {
    all: ['comments'] as const,
    lists: () => [...commentKeys.all, 'list'] as const,
    list: (postId: string) => [...commentKeys.lists(), postId] as const,
    details: () => [...commentKeys.all, 'detail'] as const,
    detail: (id: string) => [...commentKeys.details(), id] as const,
};

// Get comments for a post
export function useComments(postId: string, page = 1, size = 20) {
    return useQuery({
        queryKey: commentKeys.list(postId),
        queryFn: () => commentsApi.getComments(postId, page, size),
        enabled: !!postId,
    });
}

// Get single comment
export function useComment(id: string) {
    return useQuery({
        queryKey: commentKeys.detail(id),
        queryFn: () => commentsApi.getComment(id),
        enabled: !!id,
    });
}

// Create comment mutation
export function useCreateComment() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (data: CreateCommentRequest) => commentsApi.createComment(data),
        onSuccess: (_, { postId }) => {
            // Invalidate comments list
            queryClient.invalidateQueries({ queryKey: commentKeys.list(postId) });
            // Also invalidate post to update comment count
            queryClient.invalidateQueries({ queryKey: postKeys.detail(postId) });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}

// Update comment mutation
export function useUpdateComment() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, data }: { id: string; data: UpdateCommentRequest }) =>
            commentsApi.updateComment(id, data),
        onSuccess: (_, { id }) => {
            queryClient.invalidateQueries({ queryKey: commentKeys.detail(id) });
            queryClient.invalidateQueries({ queryKey: commentKeys.lists() });
        },
    });
}

// Delete comment mutation
export function useDeleteComment() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, postId }: { id: string; postId: string }) =>
            commentsApi.deleteComment(id),
        onSuccess: (_, { postId }) => {
            queryClient.invalidateQueries({ queryKey: commentKeys.list(postId) });
            // Also invalidate post to update comment count
            queryClient.invalidateQueries({ queryKey: postKeys.detail(postId) });
            queryClient.invalidateQueries({ queryKey: postKeys.feed() });
        },
    });
}
