'use client';

import { useMutation, useQuery, useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import * as chatApi from '@/lib/api/chat';
import type { SendMessageRequest, CreateDirectConversationRequest, CreateGroupConversationRequest } from '@/lib/types';

// Query keys
export const chatKeys = {
    all: ['chat'] as const,
    conversations: () => [...chatKeys.all, 'conversations'] as const,
    conversation: (id: string) => [...chatKeys.all, 'conversation', id] as const,
    messages: (conversationId: string) => [...chatKeys.all, 'messages', conversationId] as const,
};

// Get all conversations
export function useConversations() {
    return useQuery({
        queryKey: chatKeys.conversations(),
        queryFn: chatApi.getConversations,
        refetchInterval: 30000, // Refetch every 30 seconds
    });
}

// Get single conversation
export function useConversation(id: string) {
    return useQuery({
        queryKey: chatKeys.conversation(id),
        queryFn: () => chatApi.getConversation(id),
        enabled: !!id,
    });
}

// Get messages with infinite scroll
export function useMessages(conversationId: string) {
    return useInfiniteQuery({
        queryKey: chatKeys.messages(conversationId),
        queryFn: ({ pageParam = 1 }) => chatApi.getMessages(conversationId, pageParam, 50),
        initialPageParam: 1,
        getNextPageParam: (lastPage) => {
            if (lastPage.page < lastPage.totalPages) {
                return lastPage.page + 1;
            }
            return undefined;
        },
        enabled: !!conversationId,
        refetchInterval: 5000, // Poll for new messages every 5 seconds
    });
}

// Create direct conversation
export function useCreateDirectConversation() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (data: CreateDirectConversationRequest) =>
            chatApi.createDirectConversation(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: chatKeys.conversations() });
        },
    });
}

// Create group conversation
export function useCreateGroupConversation() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (data: CreateGroupConversationRequest) =>
            chatApi.createGroupConversation(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: chatKeys.conversations() });
        },
    });
}

// Send message mutation
export function useSendMessage() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ conversationId, data }: { conversationId: string; data: SendMessageRequest }) =>
            chatApi.sendMessage(conversationId, data),
        onSuccess: (_, { conversationId }) => {
            queryClient.invalidateQueries({ queryKey: chatKeys.messages(conversationId) });
            queryClient.invalidateQueries({ queryKey: chatKeys.conversations() });
        },
    });
}

// Mark as read mutation
export function useMarkAsRead() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (conversationId: string) => chatApi.markAsRead(conversationId),
        onSuccess: (_, conversationId) => {
            queryClient.invalidateQueries({ queryKey: chatKeys.conversation(conversationId) });
            queryClient.invalidateQueries({ queryKey: chatKeys.conversations() });
        },
    });
}
