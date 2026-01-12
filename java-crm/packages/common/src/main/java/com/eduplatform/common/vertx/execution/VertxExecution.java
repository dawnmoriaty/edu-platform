package com.eduplatform.common.vertx.execution;

import com.eduplatform.common.vertx.VertxWrapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * VertxExecution - Helper để chạy blocking code trên Vert.x worker thread
 * 
 * Vert.x event loop không được block, nên khi cần chạy code blocking (JDBC, file I/O, etc.)
 * phải dùng executeBlocking() để chuyển sang worker thread.
 * 
 * Best practices từ Vert.x docs:
 * - Event loop: KHÔNG BAO GIỜ block (no JDBC, no file I/O, no Thread.sleep)
 * - Worker pool: Dùng cho blocking operations
 * - executeBlocking(ordered=true): Đảm bảo thứ tự giữa các blocking calls
 * - executeBlocking(ordered=false): Performance tốt hơn cho independent operations
 * 
 * Usage:
 * <pre>
 * // Chạy blocking JDBC call
 * VertxExecution.blocking(() -> {
 *     return userRepository.findById(id);
 * });
 * 
 * // Chạy async (không giữ thứ tự) - performance tốt hơn
 * VertxExecution.blockingAsync(() -> {
 *     return heavyComputation();
 * });
 * 
 * // Trả về Maybe thay vì Single (nullable result)
 * VertxExecution.blockingMaybe(() -> {
 *     return userRepository.findByEmail(email);  // có thể null
 * });
 * </pre>
 */
public class VertxExecution {
    
    private static final Logger log = LoggerFactory.getLogger(VertxExecution.class);

    /**
     * Execute blocking code on worker thread (ordered)
     * 
     * Đảm bảo thứ tự thực thi giữa các blocking calls từ cùng 1 context.
     * Dùng khi cần đảm bảo consistency.
     * 
     * @param supplier blocking code to execute
     * @return Single with result (throws if null)
     */
    public static <T> Single<T> blocking(SupplierThrowable<T> supplier) {
        return Single.create(emitter -> {
            VertxWrapper.vertx().executeBlocking(() -> {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, true)  // ordered = true
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    T result = ar.result();
                    if (result != null) {
                        emitter.onSuccess(result);
                    } else {
                        emitter.onError(new NullPointerException("Blocking call returned null"));
                    }
                } else {
                    emitter.onError(ar.cause());
                }
            });
        });
    }
    
    /**
     * Execute blocking code on worker thread (async/unordered)
     * 
     * Không đảm bảo thứ tự, nhưng performance tốt hơn.
     * Dùng cho các operations độc lập.
     * 
     * @param supplier blocking code to execute
     * @return Single with result
     */
    public static <T> Single<T> blockingAsync(SupplierThrowable<T> supplier) {
        return Single.create(emitter -> {
            VertxWrapper.vertx().executeBlocking(() -> {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, false)  // ordered = false - better performance
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    T result = ar.result();
                    if (result != null) {
                        emitter.onSuccess(result);
                    } else {
                        emitter.onError(new NullPointerException("Blocking call returned null"));
                    }
                } else {
                    emitter.onError(ar.cause());
                }
            });
        });
    }
    
    /**
     * Execute blocking code that may return null (returns Maybe)
     * 
     * Dùng khi kết quả có thể null (ví dụ: findById có thể không tìm thấy)
     * 
     * @param supplier blocking code to execute
     * @return Maybe with result (empty if null)
     */
    public static <T> Maybe<T> blockingMaybe(SupplierThrowable<T> supplier) {
        return Maybe.create(emitter -> {
            VertxWrapper.vertx().executeBlocking(() -> {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, false)
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    T result = ar.result();
                    if (result != null) {
                        emitter.onSuccess(result);
                    } else {
                        emitter.onComplete();  // Maybe.empty()
                    }
                } else {
                    emitter.onError(ar.cause());
                }
            });
        });
    }
    
    /**
     * Execute blocking code with default value if null
     */
    public static <T> Single<T> blockingOrDefault(SupplierThrowable<T> supplier, T defaultValue) {
        return blockingMaybe(supplier).switchIfEmpty(Single.just(defaultValue));
    }
    
    /**
     * Execute void blocking operation
     */
    public static Completable blockingVoid(RunnableThrowable runnable) {
        return Completable.create(emitter -> {
            VertxWrapper.vertx().executeBlocking(() -> {
                try {
                    runnable.run();
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, false)
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    emitter.onComplete();
                } else {
                    emitter.onError(ar.cause());
                }
            });
        });
    }

    /**
     * Zip multiple Singles and subscribe (fire-and-forget)
     * Useful for parallel independent operations
     */
    @SafeVarargs
    public static void subscribeAll(Single<?>... singles) {
        List<Single<?>> list = java.util.Arrays.stream(singles)
                .filter(Objects::nonNull)
                .toList();
        
        if (list.isEmpty()) return;
        
        Single.zip(list, results -> "OK")
                .subscribe(
                    result -> log.debug("All operations completed"),
                    error -> log.error("Operation failed: {}", error.getMessage(), error)
                );
    }
    
    /**
     * Execute multiple blocking operations in parallel
     * Returns when ALL complete
     */
    @SafeVarargs
    public static <T> Single<List<T>> parallelBlocking(SupplierThrowable<T>... suppliers) {
        List<Single<T>> singles = java.util.Arrays.stream(suppliers)
                .filter(Objects::nonNull)
                .map(VertxExecution::blockingAsync)
                .toList();
        
        return Single.zip(singles, results -> 
            java.util.Arrays.stream(results)
                .map(r -> (T) r)
                .toList()
        );
    }
}

