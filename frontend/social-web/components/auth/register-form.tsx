'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { useRegister } from '@/hooks/use-auth';
import { registerSchema, type RegisterFormData } from '@/lib/schemas/auth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

export function RegisterForm() {
    const { mutate: registerUser, isPending, error } = useRegister();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
    });

    const onSubmit = (data: RegisterFormData) => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { confirmPassword, ...registerData } = data;
        registerUser(registerData);
    };

    return (
        <Card className="w-full max-w-md mx-auto">
            <CardHeader className="text-center">
                <CardTitle className="text-2xl">Create an account</CardTitle>
                <p className="text-zinc-500 dark:text-zinc-400 mt-1">
                    Join our community and start connecting
                </p>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    {error && (
                        <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-sm dark:bg-red-900/20 dark:border-red-800 dark:text-red-400">
                            Registration failed. Please try again.
                        </div>
                    )}

                    <Input
                        label="Full Name"
                        type="text"
                        placeholder="Enter your full name"
                        error={errors.name?.message}
                        {...register('name')}
                    />

                    <Input
                        label="Username"
                        type="text"
                        placeholder="Choose a username"
                        error={errors.username?.message}
                        {...register('username')}
                    />

                    <Input
                        label="Email"
                        type="email"
                        placeholder="Enter your email"
                        error={errors.email?.message}
                        {...register('email')}
                    />

                    <Input
                        label="Password"
                        type="password"
                        placeholder="Create a password"
                        error={errors.password?.message}
                        {...register('password')}
                    />

                    <Input
                        label="Confirm Password"
                        type="password"
                        placeholder="Confirm your password"
                        error={errors.confirmPassword?.message}
                        {...register('confirmPassword')}
                    />

                    <div className="text-sm text-zinc-500 dark:text-zinc-400">
                        By signing up, you agree to our{' '}
                        <Link href="/terms" className="text-violet-600 hover:underline">
                            Terms of Service
                        </Link>{' '}
                        and{' '}
                        <Link href="/privacy" className="text-violet-600 hover:underline">
                            Privacy Policy
                        </Link>
                    </div>

                    <Button type="submit" className="w-full" size="lg" isLoading={isPending}>
                        Create account
                    </Button>
                </form>

                <div className="mt-6 text-center text-sm text-zinc-500 dark:text-zinc-400">
                    Already have an account?{' '}
                    <Link
                        href="/login"
                        className="text-violet-600 hover:text-violet-700 font-medium dark:text-violet-400"
                    >
                        Sign in
                    </Link>
                </div>
            </CardContent>
        </Card>
    );
}
