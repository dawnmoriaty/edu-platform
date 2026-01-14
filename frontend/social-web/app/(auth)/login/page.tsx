import { LoginForm } from '@/components/auth/login-form';

export default function LoginPage() {
    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-violet-50 via-white to-indigo-50 dark:from-zinc-950 dark:via-zinc-900 dark:to-zinc-950 p-4">
            {/* Background decoration */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -right-40 w-80 h-80 bg-violet-200/50 rounded-full blur-3xl dark:bg-violet-900/20" />
                <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-indigo-200/50 rounded-full blur-3xl dark:bg-indigo-900/20" />
            </div>

            <div className="relative z-10 w-full">
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
                        Social Web
                    </h1>
                    <p className="text-zinc-500 mt-2">Connect with friends and share moments</p>
                </div>
                <LoginForm />
            </div>
        </div>
    );
}
