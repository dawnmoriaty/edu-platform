import { z } from 'zod';

// Create comment schema
export const createCommentSchema = z.object({
    content: z
        .string()
        .min(1, 'Comment is required')
        .max(2000, 'Comment must be at most 2000 characters'),
});

export type CreateCommentFormData = z.infer<typeof createCommentSchema>;

// Update comment schema
export const updateCommentSchema = z.object({
    content: z
        .string()
        .min(1, 'Comment is required')
        .max(2000, 'Comment must be at most 2000 characters'),
});

export type UpdateCommentFormData = z.infer<typeof updateCommentSchema>;
