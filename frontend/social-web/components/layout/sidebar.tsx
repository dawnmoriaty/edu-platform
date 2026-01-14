'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, MessageSquare, User, Bell, Settings, LogOut } from 'lucide-react';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { useLogout, useCurrentUser } from '@/hooks/use-auth';
import { useAuthStore } from '@/stores/auth-store';
import { cn } from '@/lib/utils';

const navItems = [
    { href: '/', icon: Home, label: 'Feed' },
    { href: '/chat', icon: MessageSquare, label: 'Messages' },
    { href: '/notifications', icon: Bell, label: 'Notifications' },
];

export function Sidebar() {
    const pathname = usePathname();
    const { user } = useAuthStore();
    const { mutate: logout, isPending } = useLogout();

    // Keep user data fresh
    useCurrentUser();

    return (
        <aside className="fixed left-0 top-0 h-screen w-64 bg-white dark:bg-zinc-900 border-r border-zinc-200 dark:border-zinc-800 flex flex-col">
            {/* Logo */}
            <div className="p-6">
                <h1 className="text-2xl font-bold bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
                    Social Web
                </h1>
            </div>

            {/* Navigation */}
            <nav className="flex-1 px-3">
                {navItems.map((item) => {
                    const Icon = item.icon;
                    const isActive = pathname === item.href;

                    return (
                        <Link
                            key={item.href}
                            href={item.href}
                            className={cn(
                                'flex items-center gap-3 px-4 py-3 rounded-xl mb-1 transition-all duration-200',
                                'hover:bg-zinc-100 dark:hover:bg-zinc-800',
                                isActive && 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-400 font-medium'
                            )}
                        >
                            <Icon className="h-5 w-5" />
                            <span>{item.label}</span>
                        </Link>
                    );
                })}

                {user && (
                    <Link
                        href={`/profile/${user.id}`}
                        className={cn(
                            'flex items-center gap-3 px-4 py-3 rounded-xl mb-1 transition-all duration-200',
                            'hover:bg-zinc-100 dark:hover:bg-zinc-800',
                            pathname.startsWith('/profile') && 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-400 font-medium'
                        )}
                    >
                        <User className="h-5 w-5" />
                        <span>Profile</span>
                    </Link>
                )}

                <Link
                    href="/settings"
                    className={cn(
                        'flex items-center gap-3 px-4 py-3 rounded-xl mb-1 transition-all duration-200',
                        'hover:bg-zinc-100 dark:hover:bg-zinc-800',
                        pathname === '/settings' && 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-400 font-medium'
                    )}
                >
                    <Settings className="h-5 w-5" />
                    <span>Settings</span>
                </Link>
            </nav>

            {/* User section */}
            {user && (
                <div className="p-3 border-t border-zinc-200 dark:border-zinc-800">
                    <div className="flex items-center gap-3 p-3 rounded-xl bg-zinc-50 dark:bg-zinc-800">
                        <Avatar src={user.avatarUrl} name={user.name} size="md" />
                        <div className="flex-1 min-w-0">
                            <p className="font-semibold text-zinc-900 dark:text-zinc-100 truncate">
                                {user.name}
                            </p>
                            <p className="text-sm text-zinc-500 truncate">@{user.username}</p>
                        </div>
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => logout()}
                            disabled={isPending}
                            className="h-8 w-8 p-0 text-zinc-400 hover:text-red-500"
                        >
                            <LogOut className="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            )}
        </aside>
    );
}
