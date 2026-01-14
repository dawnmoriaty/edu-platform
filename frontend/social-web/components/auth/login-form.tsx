'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { useLogin } from '@/hooks/use-auth';
import { loginSchema, type LoginFormData } from '@/lib/schemas/auth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

export function LoginForm() {
    const { mutate: login, isPending, error } = useLogin();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = (data: LoginFormData) => {
        login(data);
    };

    return (
        <Card className="w-full max-w-md mx-auto">
            <CardHeader className="text-center">
                <CardTitle className="text-2xl">Welcome back</CardTitle>
                <p className="text-zinc-500 dark:text-zinc-400 mt-1">
                    Sign in to your account to continue
                </p>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    {error && (
                        <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-sm dark:bg-red-900/20 dark:border-red-800 dark:text-red-400">
                            Invalid credentials. Please try again.
                        </div>
                    )}

                    <Input
                        label="Email or Username"
                        type="text"
                        placeholder="Enter your email or username"
                        error={errors.identity?.message}
                        {...register('identity')}
                    />

                    <Input
                        label="Password"
                        type="password"
                        placeholder="Enter your password"
                        error={errors.password?.message}
                        {...register('password')}
                    />

                    <div className="flex items-center justify-between text-sm">
                        <label className="flex items-center gap-2 cursor-pointer">
                            <input
                                type="checkbox"
                                className="rounded border-zinc-300 text-violet-600 focus:ring-violet-500"
                            />
                            <span className="text-zinc-600 dark:text-zinc-400">Remember me</span>
                        </label>
                        <Link
                            href="/forgot-password"
                            className="text-violet-600 hover:text-violet-700 dark:text-violet-400"
                        >
                            Forgot password?
                        </Link>
                    </div>

                    <Button type="submit" className="w-full" size="lg" isLoading={isPending}>
                        Sign in
                    </Button>
                </form>

                <div className="mt-6 text-center text-sm text-zinc-500 dark:text-zinc-400">
                    Don't have an account?{' '}
                    <Link
                        href="/register"
                        className="text-violet-600 hover:text-violet-700 font-medium dark:text-violet-400"
                    >
                        Sign up
                    </Link>
                </div>
            </CardContent>
        </Card>
    );
}
