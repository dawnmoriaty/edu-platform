'use client';

import { useState } from 'react';
import { MessageSquare } from 'lucide-react';
import { ConversationList } from './conversation-list';
import { MessageList } from './message-list';
import { MessageInput } from './message-input';
import { Avatar } from '@/components/ui/avatar';
import type { Conversation } from '@/lib/types';

export function ChatContainer() {
    const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);

    return (
        <div className="flex h-[calc(100vh-4rem)] bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 overflow-hidden">
            {/* Sidebar - Conversations */}
            <div className="w-80 border-r border-zinc-200 dark:border-zinc-800 flex flex-col">
                <div className="p-4 border-b border-zinc-200 dark:border-zinc-800">
                    <h2 className="text-xl font-bold text-zinc-900 dark:text-zinc-100">Messages</h2>
                </div>
                <div className="flex-1 overflow-y-auto">
                    <ConversationList
                        selectedId={selectedConversation?.id}
                        onSelect={setSelectedConversation}
                    />
                </div>
            </div>

            {/* Main - Messages */}
            <div className="flex-1 flex flex-col">
                {selectedConversation ? (
                    <>
                        {/* Header */}
                        <div className="flex items-center gap-3 p-4 border-b border-zinc-200 dark:border-zinc-800">
                            <Avatar
                                src={selectedConversation.avatarUrl}
                                name={selectedConversation.name || 'Chat'}
                                size="md"
                            />
                            <div>
                                <h3 className="font-semibold text-zinc-900 dark:text-zinc-100">
                                    {selectedConversation.name || 'Direct Message'}
                                </h3>
                                <p className="text-sm text-zinc-500">
                                    {selectedConversation.participants?.length || 0} participants
                                </p>
                            </div>
                        </div>

                        {/* Messages */}
                        <MessageList conversationId={selectedConversation.id} />

                        {/* Input */}
                        <MessageInput conversationId={selectedConversation.id} />
                    </>
                ) : (
                    <div className="flex-1 flex items-center justify-center">
                        <div className="text-center">
                            <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center">
                                <MessageSquare className="w-10 h-10 text-zinc-400" />
                            </div>
                            <h3 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100 mb-1">
                                Select a conversation
                            </h3>
                            <p className="text-zinc-500">
                                Choose from your existing conversations or start a new one
                            </p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
