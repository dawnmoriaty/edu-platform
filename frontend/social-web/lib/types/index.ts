// ============================================
// Auth Types
// ============================================
export interface LoginRequest {
    identity: string; // username or email
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    name: string;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken?: string;
    tokenType: string;
    expiresIn?: number;
    user?: User;
}

export interface User {
    id: string;
    username: string;
    email: string;
    name: string;
    avatarUrl?: string;
    createdAt: string;
}

// ============================================
// Post Types
// ============================================
export interface Post {
    id: string;
    userId: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    likeCount: number;
    commentCount: number;
    isLiked: boolean;
    user?: User;
}

export interface CreatePostRequest {
    content: string;
}

export interface UpdatePostRequest {
    content: string;
}

export interface ListPostsParams {
    userId?: string;
    search?: string;
    orderBy?: string;
    page?: number;
    size?: number;
}

// ============================================
// Comment Types
// ============================================
export interface Comment {
    id: string;
    postId: string;
    userId: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    user?: User;
}

export interface CreateCommentRequest {
    postId: string;
    content: string;
}

export interface UpdateCommentRequest {
    content: string;
}

// ============================================
// Like Types
// ============================================
export interface LikePostRequest {
    postId: string;
}

export interface LikeResponse {
    id: string;
    postId: string;
    userId: string;
    createdAt: string;
}

export interface LikeCountResponse {
    postId: string;
    count: number;
}

export interface LikeStatusResponse {
    postId: string;
    isLiked: boolean;
    count: number;
}

// ============================================
// Follow Types
// ============================================
export interface FollowRequest {
    userId: string;
}

export interface FollowResponse {
    id: string;
    followerId: string;
    followeeId: string;
    createdAt: string;
}

export interface FollowStatsResponse {
    userId?: string;
    followersCount: number;
    followingCount: number;
    isFollowing?: boolean;
}

// ============================================
// Chat Types
// ============================================
export interface Conversation {
    id: string;
    type: 'direct' | 'group';
    name?: string;
    avatarUrl?: string;
    lastMessage?: string;
    lastMessageAt?: string;
    createdAt: string;
    updatedAt: string;
    participants: Participant[];
}

export interface Participant {
    userId: string;
    role: 'admin' | 'member';
    joinedAt: string;
    user?: User;
}

export interface Message {
    id: string;
    conversationId: string;
    senderId: string;
    messageType: 'text' | 'image' | 'file';
    content?: string;
    mediaUrl?: string;
    status: 'sent' | 'delivered' | 'read';
    isEdited: boolean;
    createdAt: string;
    sender?: User;
}

export interface CreateDirectConversationRequest {
    recipientId: string;
}

export interface CreateGroupConversationRequest {
    name: string;
    participantIds: string[];
}

export interface SendMessageRequest {
    content: string;
    messageType?: 'text' | 'image' | 'file';
    mediaUrl?: string;
}

// ============================================
// Common Types
// ============================================
export interface PaginationParams {
    page?: number;
    size?: number;
}

export interface Page<T> {
    items: T[];
    total: number;
    page: number;
    size: number;
    totalPages: number;
}
