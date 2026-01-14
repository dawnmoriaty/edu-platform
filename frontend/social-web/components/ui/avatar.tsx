import { cn, getInitials } from '@/lib/utils';

interface AvatarProps {
    src?: string | null;
    name: string;
    size?: 'sm' | 'md' | 'lg' | 'xl';
    className?: string;
}

export function Avatar({ src, name, size = 'md', className }: AvatarProps) {
    const sizes = {
        sm: 'h-8 w-8 text-xs',
        md: 'h-10 w-10 text-sm',
        lg: 'h-14 w-14 text-base',
        xl: 'h-20 w-20 text-xl',
    };

    const initials = getInitials(name);

    if (src) {
        return (
            <img
                src={src}
                alt={name}
                className={cn(
                    'rounded-full object-cover ring-2 ring-white dark:ring-zinc-900',
                    sizes[size],
                    className
                )}
            />
        );
    }

    return (
        <div
            className={cn(
                'flex items-center justify-center rounded-full font-semibold',
                'bg-gradient-to-br from-violet-500 to-indigo-600 text-white',
                'ring-2 ring-white dark:ring-zinc-900',
                sizes[size],
                className
            )}
        >
            {initials}
        </div>
    );
}
