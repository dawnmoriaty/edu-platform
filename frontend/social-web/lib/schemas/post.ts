import { z } from 'zod';

// Create post schema
export const createPostSchema = z.object({
    content: z
        .string()
        .min(1, 'Post content is required')
        .max(5000, 'Post content must be at most 5000 characters'),
});

export type CreatePostFormData = z.infer<typeof createPostSchema>;

// Update post schema
export const updatePostSchema = z.object({
    content: z
        .string()
        .min(1, 'Post content is required')
        .max(5000, 'Post content must be at most 5000 characters'),
});

export type UpdatePostFormData = z.infer<typeof updatePostSchema>;
