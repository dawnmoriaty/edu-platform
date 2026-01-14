'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ImagePlus, Send } from 'lucide-react';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card } from '@/components/ui/card';
import { useCreatePost } from '@/hooks/use-posts';
import { useAuthStore } from '@/stores/auth-store';
import { createPostSchema, type CreatePostFormData } from '@/lib/schemas/post';

export function CreatePostForm() {
    const { user } = useAuthStore();
    const { mutate: createPost, isPending } = useCreatePost();

    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: { errors },
    } = useForm<CreatePostFormData>({
        resolver: zodResolver(createPostSchema),
        defaultValues: { content: '' },
    });

    const content = watch('content');

    const onSubmit = (data: CreatePostFormData) => {
        createPost(data, {
            onSuccess: () => {
                reset();
            },
        });
    };

    if (!user) return null;

    return (
        <Card className="p-4">
            <form onSubmit={handleSubmit(onSubmit)}>
                <div className="flex gap-3">
                    <Avatar src={user.avatarUrl} name={user.name} size="md" />
                    <div className="flex-1">
                        <Textarea
                            placeholder="What's on your mind?"
                            className="min-h-[80px] border-0 bg-zinc-50 dark:bg-zinc-800 focus:ring-0 resize-none"
                            {...register('content')}
                        />
                        {errors.content && (
                            <p className="text-sm text-red-500 mt-1">{errors.content.message}</p>
                        )}
                    </div>
                </div>

                <div className="flex items-center justify-between mt-3 pt-3 border-t border-zinc-100 dark:border-zinc-800">
                    <div className="flex gap-2">
                        <Button type="button" variant="ghost" size="sm" className="text-zinc-500">
                            <ImagePlus className="h-5 w-5 mr-1" />
                            Photo
                        </Button>
                    </div>

                    <div className="flex items-center gap-3">
                        <span className="text-sm text-zinc-400">
                            {content?.length || 0}/5000
                        </span>
                        <Button
                            type="submit"
                            size="sm"
                            disabled={!content?.trim() || isPending}
                            isLoading={isPending}
                            className="gap-1"
                        >
                            <Send className="h-4 w-4" />
                            Post
                        </Button>
                    </div>
                </div>
            </form>
        </Card>
    );
}
