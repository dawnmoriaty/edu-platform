'use client';

import { Trash2 } from 'lucide-react';
import Link from 'next/link';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { useDeleteComment } from '@/hooks/use-comments';
import { useAuthStore } from '@/stores/auth-store';
import { formatRelativeTime } from '@/lib/utils';
import type { Comment } from '@/lib/types';

interface CommentItemProps {
    comment: Comment;
    postId: string;
}

export function CommentItem({ comment, postId }: CommentItemProps) {
    const { user } = useAuthStore();
    const { mutate: deleteComment, isPending } = useDeleteComment();

    const isOwner = user?.id === comment.userId;

    const handleDelete = () => {
        if (confirm('Delete this comment?')) {
            deleteComment({ id: comment.id, postId });
        }
    };

    return (
        <div className="flex gap-3 group">
            <Link href={`/profile/${comment.userId}`}>
                <Avatar
                    src={comment.user?.avatarUrl}
                    name={comment.user?.name || 'User'}
                    size="sm"
                />
            </Link>
            <div className="flex-1 min-w-0">
                <div className="inline-block max-w-full bg-zinc-100 dark:bg-zinc-800 rounded-2xl px-4 py-2">
                    <div className="flex items-center gap-2">
                        <Link
                            href={`/profile/${comment.userId}`}
                            className="font-semibold text-sm text-zinc-900 dark:text-zinc-100 hover:underline"
                        >
                            {comment.user?.name || 'Unknown'}
                        </Link>
                        <span className="text-xs text-zinc-400">
                            {formatRelativeTime(comment.createdAt)}
                        </span>
                    </div>
                    <p className="text-sm text-zinc-700 dark:text-zinc-300 mt-0.5 break-words">
                        {comment.content}
                    </p>
                </div>

                {isOwner && (
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={handleDelete}
                        disabled={isPending}
                        className="opacity-0 group-hover:opacity-100 transition-opacity h-6 px-2 text-zinc-400 hover:text-red-500"
                    >
                        <Trash2 className="h-3 w-3" />
                    </Button>
                )}
            </div>
        </div>
    );
}
