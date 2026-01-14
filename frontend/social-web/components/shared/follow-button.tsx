'use client';

import { UserPlus, UserMinus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useToggleFollow, useIsFollowing } from '@/hooks/use-follows';
import { useAuthStore } from '@/stores/auth-store';

interface FollowButtonProps {
    userId: string;
    size?: 'sm' | 'md' | 'lg';
    variant?: 'primary' | 'outline';
}

export function FollowButton({ userId, size = 'md', variant = 'primary' }: FollowButtonProps) {
    const { user, isAuthenticated } = useAuthStore();
    const { data: isFollowing, isLoading: isCheckingFollow } = useIsFollowing(userId);
    const { toggle, isLoading: isToggling } = useToggleFollow();

    // Don't show follow button for own profile
    if (user?.id === userId) {
        return null;
    }

    // Not authenticated - could show login prompt
    if (!isAuthenticated) {
        return (
            <Button variant="outline" size={size} disabled>
                <UserPlus className="h-4 w-4 mr-1" />
                Follow
            </Button>
        );
    }

    const handleClick = () => {
        if (!isToggling) {
            toggle(userId, isFollowing ?? false);
        }
    };

    if (isCheckingFollow) {
        return (
            <Button variant="outline" size={size} disabled>
                <Loader2 className="h-4 w-4 animate-spin" />
            </Button>
        );
    }

    if (isFollowing) {
        return (
            <Button
                variant="outline"
                size={size}
                onClick={handleClick}
                disabled={isToggling}
                className="group hover:border-red-500 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20"
            >
                {isToggling ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                    <>
                        <span className="group-hover:hidden">Following</span>
                        <span className="hidden group-hover:inline-flex items-center">
                            <UserMinus className="h-4 w-4 mr-1" />
                            Unfollow
                        </span>
                    </>
                )}
            </Button>
        );
    }

    return (
        <Button
            variant={variant === 'primary' ? 'primary' : 'outline'}
            size={size}
            onClick={handleClick}
            disabled={isToggling}
            isLoading={isToggling}
        >
            <UserPlus className="h-4 w-4 mr-1" />
            Follow
        </Button>
    );
}
