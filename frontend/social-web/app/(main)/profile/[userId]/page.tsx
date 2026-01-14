'use client';

import { use } from 'react';
import { MapPin, Calendar, Link as LinkIcon } from 'lucide-react';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { PostSkeleton } from '@/components/ui/skeleton';
import { FollowButton } from '@/components/shared/follow-button';
import { PostCard } from '@/components/posts/post-card';
import { usePosts } from '@/hooks/use-posts';
import { useFollowStats } from '@/hooks/use-follows';
import { useCurrentUser } from '@/hooks/use-auth';
import { formatNumber } from '@/lib/utils';

interface ProfilePageProps {
    params: Promise<{ userId: string }>;
}

export default function ProfilePage({ params }: ProfilePageProps) {
    const { userId } = use(params);
    const { data: currentUser } = useCurrentUser();
    const { data: stats, isLoading: isLoadingStats } = useFollowStats(userId);
    const { data: postsData, isLoading: isLoadingPosts } = usePosts({ userId });

    const isOwnProfile = currentUser?.id === userId;
    const posts = postsData?.items || [];

    return (
        <div className="max-w-4xl mx-auto">
            {/* Profile Header */}
            <Card className="mb-6 overflow-hidden">
                {/* Cover */}
                <div className="h-32 bg-gradient-to-r from-violet-500 to-indigo-600" />

                {/* Profile Info */}
                <div className="px-6 pb-6">
                    <div className="flex items-end gap-4 -mt-12 mb-4">
                        <Avatar
                            src={currentUser?.avatarUrl}
                            name={currentUser?.name || 'User'}
                            size="xl"
                            className="ring-4 ring-white dark:ring-zinc-900"
                        />
                        <div className="flex-1 pt-12">
                            <div className="flex items-center justify-between">
                                <div>
                                    <h1 className="text-2xl font-bold text-zinc-900 dark:text-zinc-100">
                                        {currentUser?.name || 'Loading...'}
                                    </h1>
                                    <p className="text-zinc-500">@{currentUser?.username || 'user'}</p>
                                </div>
                                {isOwnProfile ? (
                                    <Button variant="outline">Edit Profile</Button>
                                ) : (
                                    <FollowButton userId={userId} size="md" />
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Bio */}
                    <p className="text-zinc-700 dark:text-zinc-300 mb-4">
                        Software developer passionate about building great products. 🚀
                    </p>

                    {/* Meta */}
                    <div className="flex flex-wrap gap-4 text-sm text-zinc-500">
                        <span className="inline-flex items-center gap-1">
                            <MapPin className="h-4 w-4" />
                            San Francisco, CA
                        </span>
                        <span className="inline-flex items-center gap-1">
                            <LinkIcon className="h-4 w-4" />
                            <a href="#" className="text-violet-600 hover:underline">example.com</a>
                        </span>
                        <span className="inline-flex items-center gap-1">
                            <Calendar className="h-4 w-4" />
                            Joined January 2024
                        </span>
                    </div>

                    {/* Stats */}
                    <div className="flex gap-6 mt-4 pt-4 border-t border-zinc-100 dark:border-zinc-800">
                        <button className="hover:underline">
                            <span className="font-bold text-zinc-900 dark:text-zinc-100">
                                {formatNumber(stats?.followingCount || 0)}
                            </span>
                            <span className="text-zinc-500 ml-1">Following</span>
                        </button>
                        <button className="hover:underline">
                            <span className="font-bold text-zinc-900 dark:text-zinc-100">
                                {formatNumber(stats?.followersCount || 0)}
                            </span>
                            <span className="text-zinc-500 ml-1">Followers</span>
                        </button>
                        <div>
                            <span className="font-bold text-zinc-900 dark:text-zinc-100">
                                {formatNumber(posts.length)}
                            </span>
                            <span className="text-zinc-500 ml-1">Posts</span>
                        </div>
                    </div>
                </div>
            </Card>

            {/* Posts */}
            <div className="space-y-4">
                <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                    Posts
                </h2>

                {isLoadingPosts ? (
                    <div className="space-y-4">
                        <PostSkeleton />
                        <PostSkeleton />
                    </div>
                ) : posts.length > 0 ? (
                    posts.map((post) => <PostCard key={post.id} post={post} />)
                ) : (
                    <Card className="py-12 text-center text-zinc-400">
                        No posts yet
                    </Card>
                )}
            </div>
        </div>
    );
}
