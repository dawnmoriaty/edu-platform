'use client';

import { Heart, MessageCircle, Share2, MoreHorizontal, Trash2, Edit } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { useToggleLike } from '@/hooks/use-likes';
import { useDeletePost } from '@/hooks/use-posts';
import { useAuthStore } from '@/stores/auth-store';
import { formatRelativeTime, formatNumber } from '@/lib/utils';
import type { Post } from '@/lib/types';

interface PostCardProps {
    post: Post;
    onEdit?: (post: Post) => void;
}

export function PostCard({ post, onEdit }: PostCardProps) {
    const [showMenu, setShowMenu] = useState(false);
    const { user } = useAuthStore();
    const { toggle: toggleLike, isLoading: isLikeLoading } = useToggleLike();
    const { mutate: deletePost, isPending: isDeleting } = useDeletePost();

    const isOwner = user?.id === post.userId;

    const handleLike = () => {
        if (!isLikeLoading) {
            toggleLike(post.id, post.isLiked);
        }
    };

    const handleDelete = () => {
        if (confirm('Are you sure you want to delete this post?')) {
            deletePost(post.id);
        }
        setShowMenu(false);
    };

    return (
        <Card hover className="transition-all duration-300">
            <CardContent className="p-0">
                {/* Header */}
                <div className="flex items-center justify-between p-4 pb-3">
                    <Link
                        href={`/profile/${post.userId}`}
                        className="flex items-center gap-3 group"
                    >
                        <Avatar
                            src={post.user?.avatarUrl}
                            name={post.user?.name || 'User'}
                            size="md"
                        />
                        <div>
                            <p className="font-semibold text-zinc-900 dark:text-zinc-100 group-hover:text-violet-600 transition-colors">
                                {post.user?.name || 'Unknown User'}
                            </p>
                            <p className="text-sm text-zinc-500 dark:text-zinc-400">
                                @{post.user?.username || 'user'} · {formatRelativeTime(post.createdAt)}
                            </p>
                        </div>
                    </Link>

                    {isOwner && (
                        <div className="relative">
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => setShowMenu(!showMenu)}
                                className="h-8 w-8 p-0"
                            >
                                <MoreHorizontal className="h-5 w-5" />
                            </Button>

                            {showMenu && (
                                <>
                                    <div
                                        className="fixed inset-0 z-10"
                                        onClick={() => setShowMenu(false)}
                                    />
                                    <div className="absolute right-0 top-full mt-1 w-36 rounded-xl bg-white shadow-lg border border-zinc-200 overflow-hidden z-20 dark:bg-zinc-800 dark:border-zinc-700">
                                        <button
                                            onClick={() => {
                                                onEdit?.(post);
                                                setShowMenu(false);
                                            }}
                                            className="flex items-center gap-2 w-full px-4 py-2.5 text-sm text-zinc-700 hover:bg-zinc-50 dark:text-zinc-300 dark:hover:bg-zinc-700"
                                        >
                                            <Edit className="h-4 w-4" />
                                            Edit
                                        </button>
                                        <button
                                            onClick={handleDelete}
                                            disabled={isDeleting}
                                            className="flex items-center gap-2 w-full px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                            Delete
                                        </button>
                                    </div>
                                </>
                            )}
                        </div>
                    )}
                </div>

                {/* Content */}
                <div className="px-4 pb-3">
                    <p className="text-zinc-800 dark:text-zinc-200 whitespace-pre-wrap leading-relaxed">
                        {post.content}
                    </p>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-1 px-2 py-2 border-t border-zinc-100 dark:border-zinc-800">
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={handleLike}
                        disabled={isLikeLoading}
                        className={`gap-2 ${post.isLiked ? 'text-red-500 hover:text-red-600' : ''}`}
                    >
                        <Heart className={`h-5 w-5 ${post.isLiked ? 'fill-current' : ''}`} />
                        <span>{formatNumber(post.likeCount)}</span>
                    </Button>

                    <Link href={`/posts/${post.id}`}>
                        <Button variant="ghost" size="sm" className="gap-2">
                            <MessageCircle className="h-5 w-5" />
                            <span>{formatNumber(post.commentCount)}</span>
                        </Button>
                    </Link>

                    <Button variant="ghost" size="sm" className="gap-2">
                        <Share2 className="h-5 w-5" />
                        <span>Share</span>
                    </Button>
                </div>
            </CardContent>
        </Card>
    );
}
