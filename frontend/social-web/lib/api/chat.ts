import { socialApi, type ApiResponse, type PageResponse } from './client';
import type {
    Conversation,
    Message,
    CreateDirectConversationRequest,
    CreateGroupConversationRequest,
    SendMessageRequest
} from '@/lib/types';

// Get all conversations for current user
export async function getConversations(): Promise<Conversation[]> {
    const response = await socialApi.get<ApiResponse<Conversation[]>>('/conversations');
    return response.data.data;
}

// Get single conversation
export async function getConversation(id: string): Promise<Conversation> {
    const response = await socialApi.get<ApiResponse<Conversation>>(`/conversations/${id}`);
    return response.data.data;
}

// Create direct conversation (1:1)
export async function createDirectConversation(
    data: CreateDirectConversationRequest
): Promise<Conversation> {
    const response = await socialApi.post<ApiResponse<Conversation>>(
        '/conversations/direct',
        data
    );
    return response.data.data;
}

// Create group conversation
export async function createGroupConversation(
    data: CreateGroupConversationRequest
): Promise<Conversation> {
    const response = await socialApi.post<ApiResponse<Conversation>>(
        '/conversations/group',
        data
    );
    return response.data.data;
}

// Get messages in a conversation
export async function getMessages(
    conversationId: string,
    page = 1,
    size = 50
): Promise<PageResponse<Message>> {
    const response = await socialApi.get<ApiResponse<PageResponse<Message>>>(
        `/conversations/${conversationId}/messages`,
        { params: { page, size } }
    );
    return response.data.data;
}

// Send a message
export async function sendMessage(
    conversationId: string,
    data: SendMessageRequest
): Promise<Message> {
    const response = await socialApi.post<ApiResponse<Message>>(
        `/conversations/${conversationId}/messages`,
        data
    );
    return response.data.data;
}

// Mark messages as read
export async function markAsRead(conversationId: string): Promise<void> {
    await socialApi.post(`/conversations/${conversationId}/read`);
}
