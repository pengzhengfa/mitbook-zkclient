package com.mitbook.zkclient.util;

import com.mitbook.zkclient.exception.ZKInterruptedException;

/**
 * @author pengzhengfa
 */
public class ExceptionUtil {

    public static RuntimeException convertToRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        retainInterruptFlag(e);
        return new RuntimeException(e);
    }

    /**
     * 如果catch {@link InterruptedException}异常,重置当前线程interrupt标记
     *
     * @param catchedException
     */
    public static void retainInterruptFlag(Throwable catchedException) {
        if (catchedException instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public static void rethrowInterruptedException(Throwable e) throws InterruptedException {
        if (e instanceof InterruptedException) {
            throw (InterruptedException) e;
        }
        if (e instanceof ZKInterruptedException) {
            throw (ZKInterruptedException) e;
        }
    }
}
