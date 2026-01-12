package com.eduplatform.common.vertx.execution;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * WorkerPoolManager - Quản lý các Worker Pool riêng biệt
 * 
 * Phân loại Worker Pool theo loại công việc:
 * - DB Pool: Cho các truy vấn database (JDBC/jOOQ)
 * - IO Pool: Cho các I/O operations (file, external API)
 * - CPU Pool: Cho các tính toán nặng
 * 
 * Việc tách biệt giúp tránh tình trạng một nhóm tác vụ chậm
 * làm nghẽn toàn bộ hệ thống.
 * 
 * Usage:
 * <pre>
 * // Dùng DB pool cho queries
 * WorkerPoolManager.executeOnDbPool(() -> userRepository.findById(id));
 * 
 * // Dùng IO pool cho external calls
 * WorkerPoolManager.executeOnIoPool(() -> httpClient.get(url));
 * </pre>
 */
@Slf4j
@Component
public class WorkerPoolManager {
    
    public static final String DB_POOL = "db-pool";
    public static final String IO_POOL = "io-pool";
    public static final String CPU_POOL = "cpu-pool";
    
    private static WorkerPoolManager INSTANCE;
    
    private final ConcurrentHashMap<String, WorkerExecutor> pools = new ConcurrentHashMap<>();
    
    private Vertx vertx;
    
    // Giảm default cho máy dev (2 cores / 4 threads)
    @Value("${vertx.worker.db-pool-size:8}")
    private int dbPoolSize;
    
    @Value("${vertx.worker.io-pool-size:4}")
    private int ioPoolSize;
    
    @Value("${vertx.worker.cpu-pool-size:2}")
    private int cpuPoolSize;
    
    @Value("${vertx.worker.max-execute-time:30}")
    private long maxExecuteTimeSeconds;
    
    @PostConstruct
    public void init() {
        INSTANCE = this;
        log.info("WorkerPoolManager initialized with pools: DB={}, IO={}, CPU={}", 
                dbPoolSize, ioPoolSize, cpuPoolSize);
    }
    
    /**
     * Initialize pools with Vertx instance
     * Call this after Vertx is available (e.g., in VertxRoutingBinder.bind())
     */
    public void initPools(Vertx vertx) {
        this.vertx = vertx;
        
        // Database pool - cho JDBC/jOOQ queries
        // Size lớn hơn vì DB operations thường blocking lâu
        pools.put(DB_POOL, vertx.createSharedWorkerExecutor(
                DB_POOL,
                dbPoolSize,
                maxExecuteTimeSeconds,
                TimeUnit.SECONDS
        ));
        
        // IO pool - cho external HTTP calls, file I/O
        pools.put(IO_POOL, vertx.createSharedWorkerExecutor(
                IO_POOL,
                ioPoolSize,
                maxExecuteTimeSeconds,
                TimeUnit.SECONDS
        ));
        
        // CPU pool - cho heavy computation
        // Size nhỏ = số cores để tránh context switching
        pools.put(CPU_POOL, vertx.createSharedWorkerExecutor(
                CPU_POOL,
                cpuPoolSize,
                maxExecuteTimeSeconds * 2,  // CPU ops có thể mất nhiều thời gian hơn
                TimeUnit.SECONDS
        ));
        
        log.info("Created worker pools: DB({}), IO({}), CPU({})", 
                dbPoolSize, ioPoolSize, cpuPoolSize);
    }
    
    @PreDestroy
    public void shutdown() {
        pools.values().forEach(WorkerExecutor::close);
        pools.clear();
        log.info("Worker pools shutdown completed");
    }
    
    /**
     * Get a specific worker pool
     */
    public WorkerExecutor getPool(String name) {
        return pools.get(name);
    }
    
    /**
     * Get DB worker pool
     */
    public WorkerExecutor getDbPool() {
        return pools.get(DB_POOL);
    }
    
    /**
     * Get IO worker pool
     */
    public WorkerExecutor getIoPool() {
        return pools.get(IO_POOL);
    }
    
    /**
     * Get CPU worker pool
     */
    public WorkerExecutor getCpuPool() {
        return pools.get(CPU_POOL);
    }
    
    // ============================================
    // Static convenience methods
    // ============================================
    
    public static WorkerPoolManager instance() {
        return INSTANCE;
    }
    
    /**
     * Execute on DB pool - dùng cho tất cả database operations
     */
    public static <T> io.reactivex.rxjava3.core.Single<T> executeOnDbPool(SupplierThrowable<T> supplier) {
        return executeOn(DB_POOL, supplier);
    }
    
    /**
     * Execute on IO pool - dùng cho external HTTP calls, file I/O
     */
    public static <T> io.reactivex.rxjava3.core.Single<T> executeOnIoPool(SupplierThrowable<T> supplier) {
        return executeOn(IO_POOL, supplier);
    }
    
    /**
     * Execute on CPU pool - dùng cho heavy computation
     */
    public static <T> io.reactivex.rxjava3.core.Single<T> executeOnCpuPool(SupplierThrowable<T> supplier) {
        return executeOn(CPU_POOL, supplier);
    }
    
    /**
     * Execute on specified pool
     */
    public static <T> io.reactivex.rxjava3.core.Single<T> executeOn(String poolName, SupplierThrowable<T> supplier) {
        if (INSTANCE == null || INSTANCE.pools.get(poolName) == null) {
            // Fallback to default executeBlocking if pools not initialized
            return VertxExecution.blocking(supplier);
        }
        
        WorkerExecutor executor = INSTANCE.pools.get(poolName);
        
        return io.reactivex.rxjava3.core.Single.create(emitter -> {
            executor.executeBlocking(() -> {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, false)  // unordered for better performance
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    T result = ar.result();
                    if (result != null) {
                        emitter.onSuccess(result);
                    } else {
                        emitter.onError(new NullPointerException("Operation returned null"));
                    }
                } else {
                    emitter.onError(ar.cause());
                }
            });
        });
    }
}
