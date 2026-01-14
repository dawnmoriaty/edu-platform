import { CreatePostForm } from '@/components/posts/create-post-form';
import { PostList } from '@/components/posts/post-list';

export default function FeedPage() {
    return (
        <div className="max-w-2xl mx-auto">
            {/* Page Header */}
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-zinc-900 dark:text-zinc-100">
                    Feed
                </h1>
                <p className="text-zinc-500 mt-1">
                    See what's happening with people you follow
                </p>
            </div>

            {/* Create Post */}
            <div className="mb-6">
                <CreatePostForm />
            </div>

            {/* Posts */}
            <PostList />
        </div>
    );
}
