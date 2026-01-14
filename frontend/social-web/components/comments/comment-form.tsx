'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Send } from 'lucide-react';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { useCreateComment } from '@/hooks/use-comments';
import { useAuthStore } from '@/stores/auth-store';
import { createCommentSchema, type CreateCommentFormData } from '@/lib/schemas/comment';

interface CommentFormProps {
    postId: string;
}

export function CommentForm({ postId }: CommentFormProps) {
    const { user } = useAuthStore();
    const { mutate: createComment, isPending } = useCreateComment();

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<CreateCommentFormData>({
        resolver: zodResolver(createCommentSchema),
    });

    const onSubmit = (data: CreateCommentFormData) => {
        createComment(
            { postId, content: data.content },
            { onSuccess: () => reset() }
        );
    };

    if (!user) return null;

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="flex gap-3">
            <Avatar src={user.avatarUrl} name={user.name} size="sm" />
            <div className="flex-1 flex gap-2">
                <div className="flex-1">
                    <input
                        type="text"
                        placeholder="Write a comment..."
                        className="w-full h-10 px-4 rounded-full bg-zinc-100 dark:bg-zinc-800 border-0 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500"
                        {...register('content')}
                    />
                    {errors.content && (
                        <p className="text-xs text-red-500 mt-1 ml-4">{errors.content.message}</p>
                    )}
                </div>
                <Button
                    type="submit"
                    size="sm"
                    disabled={isPending}
                    isLoading={isPending}
                    className="h-10 w-10 p-0 rounded-full"
                >
                    <Send className="h-4 w-4" />
                </Button>
            </div>
        </form>
    );
}
