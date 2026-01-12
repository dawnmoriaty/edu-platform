package com.eduplatform.common.vertx.execution;

/**
 * RunnableThrowable - Runnable that can throw checked exceptions
 */
@FunctionalInterface
public interface RunnableThrowable {
    void run() throws Exception;
}
