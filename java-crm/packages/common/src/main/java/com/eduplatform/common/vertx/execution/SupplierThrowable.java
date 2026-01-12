package com.eduplatform.common.vertx.execution;

/**
 * SupplierThrowable - Supplier that can throw checked exceptions
 * 
 * Dùng với VertxExecution.blocking() để wrap JDBC/IO calls
 * 
 * Usage:
 * <pre>
 * VertxExecution.blocking(() -> {
 *     return userRepository.findById(id);  // có thể throw SQLException
 * });
 * </pre>
 */
@FunctionalInterface
public interface SupplierThrowable<T> {
    T get() throws Exception;
}
