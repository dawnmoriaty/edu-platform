'use client';

import { Avatar } from '@/components/ui/avatar';
import { ConversationSkeleton } from '@/components/ui/skeleton';
import { useConversations } from '@/hooks/use-chat';
import { formatRelativeTime, cn } from '@/lib/utils';
import type { Conversation } from '@/lib/types';

interface ConversationListProps {
    selectedId?: string;
    onSelect: (conversation: Conversation) => void;
}

export function ConversationList({ selectedId, onSelect }: ConversationListProps) {
    const { data: conversations, isLoading, isError } = useConversations();

    if (isLoading) {
        return (
            <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
                {Array.from({ length: 5 }).map((_, i) => (
                    <ConversationSkeleton key={i} />
                ))}
            </div>
        );
    }

    if (isError) {
        return (
            <div className="text-center py-8 text-red-500">
                Failed to load conversations
            </div>
        );
    }

    if (!conversations?.length) {
        return (
            <div className="text-center py-12 text-zinc-400">
                <p>No conversations yet</p>
                <p className="text-sm mt-1">Start chatting with someone!</p>
            </div>
        );
    }

    return (
        <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
            {conversations.map((conversation) => (
                <button
                    key={conversation.id}
                    onClick={() => onSelect(conversation)}
                    className={cn(
                        'w-full flex items-center gap-3 p-3 text-left transition-colors',
                        'hover:bg-zinc-50 dark:hover:bg-zinc-800/50',
                        selectedId === conversation.id && 'bg-violet-50 dark:bg-violet-900/20'
                    )}
                >
                    <Avatar
                        src={conversation.avatarUrl}
                        name={conversation.name || 'Chat'}
                        size="lg"
                    />
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-2">
                            <h3 className="font-semibold text-zinc-900 dark:text-zinc-100 truncate">
                                {conversation.name || 'Direct Message'}
                            </h3>
                            {conversation.lastMessageAt && (
                                <span className="text-xs text-zinc-400 flex-shrink-0">
                                    {formatRelativeTime(conversation.lastMessageAt)}
                                </span>
                            )}
                        </div>
                        {conversation.lastMessage && (
                            <p className="text-sm text-zinc-500 truncate mt-0.5">
                                {conversation.lastMessage}
                            </p>
                        )}
                    </div>
                </button>
            ))}
        </div>
    );
}
