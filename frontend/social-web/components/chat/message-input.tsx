'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Send, Image, Paperclip } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useSendMessage } from '@/hooks/use-chat';
import { sendMessageSchema, type SendMessageFormData } from '@/lib/schemas/chat';

interface MessageInputProps {
    conversationId: string;
}

export function MessageInput({ conversationId }: MessageInputProps) {
    const { mutate: sendMessage, isPending } = useSendMessage();

    const {
        register,
        handleSubmit,
        reset,
    } = useForm<SendMessageFormData>({
        resolver: zodResolver(sendMessageSchema),
        defaultValues: { content: '' },
    });

    const onSubmit = (data: SendMessageFormData) => {
        sendMessage(
            {
                conversationId,
                data: { content: data.content, messageType: 'text' }
            },
            { onSuccess: () => reset() }
        );
    };

    return (
        <form
            onSubmit={handleSubmit(onSubmit)}
            className="flex items-center gap-2 p-4 border-t border-zinc-200 dark:border-zinc-800"
        >
            <Button type="button" variant="ghost" size="sm" className="flex-shrink-0">
                <Image className="h-5 w-5" />
            </Button>
            <Button type="button" variant="ghost" size="sm" className="flex-shrink-0">
                <Paperclip className="h-5 w-5" />
            </Button>

            <input
                type="text"
                placeholder="Type a message..."
                className="flex-1 h-10 px-4 rounded-full bg-zinc-100 dark:bg-zinc-800 border-0 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500"
                {...register('content')}
            />

            <Button
                type="submit"
                disabled={isPending}
                isLoading={isPending}
                className="flex-shrink-0 h-10 w-10 p-0 rounded-full"
            >
                <Send className="h-5 w-5" />
            </Button>
        </form>
    );
}
