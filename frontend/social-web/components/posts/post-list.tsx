'use client';

import { useInView } from 'react-intersection-observer';
import { useEffect } from 'react';
import { PostCard } from './post-card';
import { PostSkeleton } from '@/components/ui/skeleton';
import { useFeed } from '@/hooks/use-posts';
import type { Post } from '@/lib/types';

interface PostListProps {
    onEditPost?: (post: Post) => void;
}

export function PostList({ onEditPost }: PostListProps) {
    const {
        data,
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage,
        isLoading,
        isError,
        error,
    } = useFeed();

    const { ref, inView } = useInView({
        threshold: 0,
        rootMargin: '100px',
    });

    // Fetch next page when sentinel is in view
    useEffect(() => {
        if (inView && hasNextPage && !isFetchingNextPage) {
            fetchNextPage();
        }
    }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage]);

    if (isLoading) {
        return (
            <div className="space-y-4">
                {Array.from({ length: 3 }).map((_, i) => (
                    <PostSkeleton key={i} />
                ))}
            </div>
        );
    }

    if (isError) {
        return (
            <div className="text-center py-12">
                <p className="text-red-500">Error loading posts: {error.message}</p>
            </div>
        );
    }

    const posts = data?.pages.flatMap((page) => page.items) || [];

    if (posts.length === 0) {
        return (
            <div className="text-center py-12">
                <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-zinc-100 dark:bg-zinc-800 flex items-center justify-center">
                    <svg className="w-10 h-10 text-zinc-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                    </svg>
                </div>
                <h3 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100 mb-1">
                    No posts yet
                </h3>
                <p className="text-zinc-500 dark:text-zinc-400">
                    Follow some people or create your first post!
                </p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {posts.map((post) => (
                <PostCard key={post.id} post={post} onEdit={onEditPost} />
            ))}

            {/* Infinite scroll sentinel */}
            <div ref={ref} className="h-4" />

            {isFetchingNextPage && (
                <div className="space-y-4">
                    <PostSkeleton />
                    <PostSkeleton />
                </div>
            )}

            {!hasNextPage && posts.length > 0 && (
                <p className="text-center text-zinc-400 py-8">
                    You've reached the end! 🎉
                </p>
            )}
        </div>
    );
}
