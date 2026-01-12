package com.eduplatform.common.vertx.execution;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * RxUtils - RxJava utility methods
 * 
 * Helper methods cho common RxJava patterns
 */
@Slf4j
public class RxUtils {

    /**
     * Convert Maybe to Single with default value
     * Useful when you have Maybe but need Single for API response
     */
    public static <T> Single<T> maybeToSingle(Maybe<T> maybe, T defaultValue) {
        return maybe.switchIfEmpty(Single.just(defaultValue));
    }

    /**
     * Convert Maybe to Single, throw if empty
     */
    public static <T> Single<T> maybeToSingle(Maybe<T> maybe, Supplier<Throwable> errorSupplier) {
        return maybe.switchIfEmpty(Single.error(errorSupplier.get()));
    }

    /**
     * Convert Optional to Single
     */
    public static <T> Single<T> optionalToSingle(Optional<T> optional, Supplier<Throwable> errorSupplier) {
        return optional.map(Single::just)
                .orElseGet(() -> Single.error(errorSupplier.get()));
    }

    /**
     * Convert Optional to Maybe
     */
    public static <T> Maybe<T> optionalToMaybe(Optional<T> optional) {
        return optional.map(Maybe::just).orElse(Maybe.empty());
    }

    /**
     * Wrap a nullable value in Maybe
     */
    public static <T> Maybe<T> nullable(T value) {
        return value != null ? Maybe.just(value) : Maybe.empty();
    }

    /**
     * Create Single from supplier, catching exceptions
     */
    public static <T> Single<T> fromSupplier(Supplier<T> supplier) {
        return Single.fromCallable(supplier::get);
    }

    /**
     * Create Completable from runnable, catching exceptions
     */
    public static Completable fromRunnable(Runnable runnable) {
        return Completable.fromAction(runnable::run);
    }

    /**
     * Log error but continue with fallback
     */
    public static <T> Function<Throwable, Single<T>> logAndFallback(String operation, T fallback) {
        return error -> {
            log.error("Error in {}: {}", operation, error.getMessage());
            return Single.just(fallback);
        };
    }

    /**
     * Log error and rethrow
     */
    public static <T> Function<Throwable, Single<T>> logAndRethrow(String operation) {
        return error -> {
            log.error("Error in {}: {}", operation, error.getMessage(), error);
            return Single.error(error);
        };
    }

    /**
     * Retry with exponential backoff
     */
    public static <T> Single<T> retryWithBackoff(Single<T> source, int maxRetries, long initialDelayMs) {
        return source.retryWhen(errors -> errors
                .zipWith(io.reactivex.rxjava3.core.Flowable.range(1, maxRetries), (error, retryCount) -> {
                    if (retryCount >= maxRetries) {
                        throw new RuntimeException("Max retries exceeded", error);
                    }
                    long delay = initialDelayMs * (long) Math.pow(2, retryCount - 1);
                    log.warn("Retry {} after {}ms due to: {}", retryCount, delay, error.getMessage());
                    return retryCount;
                })
                .flatMap(retryCount -> {
                    long delay = initialDelayMs * (long) Math.pow(2, retryCount - 1);
                    return io.reactivex.rxjava3.core.Flowable.timer(delay, java.util.concurrent.TimeUnit.MILLISECONDS);
                })
        );
    }
}
