'use client';

import { CommentItem } from './comment-item';
import { CommentForm } from './comment-form';
import { CommentSkeleton } from '@/components/ui/skeleton';
import { useComments } from '@/hooks/use-comments';

interface CommentListProps {
    postId: string;
}

export function CommentList({ postId }: CommentListProps) {
    const { data, isLoading, isError } = useComments(postId);

    return (
        <div className="space-y-4">
            <CommentForm postId={postId} />

            {isLoading && (
                <div className="space-y-3">
                    <CommentSkeleton />
                    <CommentSkeleton />
                    <CommentSkeleton />
                </div>
            )}

            {isError && (
                <p className="text-sm text-red-500">Failed to load comments</p>
            )}

            {data && data.items.length > 0 && (
                <div className="space-y-3">
                    {data.items.map((comment) => (
                        <CommentItem key={comment.id} comment={comment} postId={postId} />
                    ))}
                </div>
            )}

            {data && data.items.length === 0 && (
                <p className="text-sm text-zinc-400 text-center py-4">
                    No comments yet. Be the first to comment!
                </p>
            )}
        </div>
    );
}
