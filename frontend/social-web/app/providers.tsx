'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState, type ReactNode } from 'react';

// Create QueryClient with default options
function makeQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {
                // Stale time - data considered fresh for 1 minute
                staleTime: 60 * 1000,
                // Retry failed queries 1 time
                retry: 1,
                // Refetch on window focus
                refetchOnWindowFocus: false,
            },
            mutations: {
                // Retry failed mutations 0 times
                retry: 0,
            },
        },
    });
}

let browserQueryClient: QueryClient | undefined = undefined;

function getQueryClient() {
    if (typeof window === 'undefined') {
        // Server: always make a new query client
        return makeQueryClient();
    } else {
        // Browser: make a new query client if we don't already have one
        if (!browserQueryClient) browserQueryClient = makeQueryClient();
        return browserQueryClient;
    }
}

interface ProvidersProps {
    children: ReactNode;
}

export function Providers({ children }: ProvidersProps) {
    const queryClient = getQueryClient();

    return (
        <QueryClientProvider client={queryClient}>
            {children}
        </QueryClientProvider>
    );
}
