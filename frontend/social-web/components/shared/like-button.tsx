'use client';

import { Heart } from 'lucide-react';
import { useToggleLike } from '@/hooks/use-likes';
import { useAuthStore } from '@/stores/auth-store';
import { cn, formatNumber } from '@/lib/utils';

interface LikeButtonProps {
    postId: string;
    isLiked: boolean;
    likeCount: number;
    size?: 'sm' | 'md' | 'lg';
    showCount?: boolean;
}

export function LikeButton({
    postId,
    isLiked,
    likeCount,
    size = 'md',
    showCount = true
}: LikeButtonProps) {
    const { isAuthenticated } = useAuthStore();
    const { toggle, isLoading } = useToggleLike();

    const sizes = {
        sm: 'h-4 w-4',
        md: 'h-5 w-5',
        lg: 'h-6 w-6',
    };

    const handleClick = () => {
        if (!isAuthenticated) {
            // Could redirect to login or show a modal
            return;
        }
        if (!isLoading) {
            toggle(postId, isLiked);
        }
    };

    return (
        <button
            onClick={handleClick}
            disabled={isLoading || !isAuthenticated}
            className={cn(
                'inline-flex items-center gap-1.5 transition-all duration-200',
                'hover:scale-110 active:scale-95',
                'disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100',
                isLiked ? 'text-red-500' : 'text-zinc-500 hover:text-red-500'
            )}
        >
            <Heart
                className={cn(
                    sizes[size],
                    'transition-all duration-200',
                    isLiked && 'fill-current animate-pulse'
                )}
            />
            {showCount && (
                <span className="text-sm font-medium">
                    {formatNumber(likeCount)}
                </span>
            )}
        </button>
    );
}
