'use client';

import { useEffect, useRef } from 'react';
import { Avatar } from '@/components/ui/avatar';
import { Skeleton } from '@/components/ui/skeleton';
import { useMessages } from '@/hooks/use-chat';
import { useAuthStore } from '@/stores/auth-store';
import { formatRelativeTime, cn } from '@/lib/utils';

interface MessageListProps {
    conversationId: string;
}

export function MessageList({ conversationId }: MessageListProps) {
    const { user } = useAuthStore();
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } = useMessages(conversationId);

    const messages = data?.pages.flatMap((page) => page.items).reverse() || [];

    // Scroll to bottom on new messages
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages.length]);

    if (isLoading) {
        return (
            <div className="flex-1 p-4 space-y-4 overflow-y-auto">
                {Array.from({ length: 5 }).map((_, i) => (
                    <div key={i} className="flex gap-3">
                        <Skeleton className="h-8 w-8 rounded-full flex-shrink-0" />
                        <Skeleton className="h-16 w-48 rounded-2xl" />
                    </div>
                ))}
            </div>
        );
    }

    return (
        <div className="flex-1 overflow-y-auto p-4">
            {/* Load more button */}
            {hasNextPage && (
                <button
                    onClick={() => fetchNextPage()}
                    disabled={isFetchingNextPage}
                    className="w-full py-2 text-sm text-violet-600 hover:text-violet-700 disabled:opacity-50"
                >
                    {isFetchingNextPage ? 'Loading...' : 'Load older messages'}
                </button>
            )}

            <div className="space-y-4">
                {messages.map((message) => {
                    const isOwn = message.senderId === user?.id;

                    return (
                        <div
                            key={message.id}
                            className={cn('flex gap-3', isOwn && 'flex-row-reverse')}
                        >
                            {!isOwn && (
                                <Avatar
                                    src={message.sender?.avatarUrl}
                                    name={message.sender?.name || 'User'}
                                    size="sm"
                                />
                            )}
                            <div className={cn('max-w-[70%]', isOwn && 'text-right')}>
                                <div
                                    className={cn(
                                        'inline-block rounded-2xl px-4 py-2',
                                        isOwn
                                            ? 'bg-gradient-to-r from-violet-600 to-indigo-600 text-white'
                                            : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100'
                                    )}
                                >
                                    <p className="text-sm whitespace-pre-wrap break-words">
                                        {message.content}
                                    </p>
                                </div>
                                <p className="text-xs text-zinc-400 mt-1 px-1">
                                    {formatRelativeTime(message.createdAt)}
                                </p>
                            </div>
                        </div>
                    );
                })}
            </div>

            <div ref={messagesEndRef} />
        </div>
    );
}
