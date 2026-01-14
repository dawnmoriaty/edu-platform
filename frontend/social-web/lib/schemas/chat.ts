import { z } from 'zod';

// Send message schema
export const sendMessageSchema = z.object({
    content: z
        .string()
        .min(1, 'Message is required')
        .max(5000, 'Message is too long'),
    messageType: z
        .enum(['text', 'image', 'file'])
        .default('text'),
});

export type SendMessageFormData = z.input<typeof sendMessageSchema>;

// Create group conversation schema
export const createGroupSchema = z.object({
    name: z
        .string()
        .min(1, 'Group name is required')
        .max(100, 'Group name is too long'),
    participantIds: z
        .array(z.string().uuid())
        .min(1, 'At least one participant is required'),
});

export type CreateGroupFormData = z.infer<typeof createGroupSchema>;
