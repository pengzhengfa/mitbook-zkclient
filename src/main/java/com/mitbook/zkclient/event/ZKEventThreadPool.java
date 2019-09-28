package com.mitbook.zkclient.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件处理线程池
 * 阻塞队列无限制
 *
 * @author pengzhengfa
 */
public class ZKEventThreadPool {
    private static Logger LOG = LoggerFactory.getLogger(ZKEventThreadPool.class);
    private static AtomicInteger index = new AtomicInteger(0);
    private ThreadPoolExecutor pool = null;

    /**
     * 初始化线程池,这里采用corePoolSize=maximumPoolSize,
     * 并且使用LinkedBlockingQueue无限大小的阻塞队列来处理事件
     * ZKEventThreadPoolExecutor.
     *
     * @param poolSize
     */
    public ZKEventThreadPool(Integer poolSize) {
        pool = new ThreadPoolExecutor(
                poolSize,         //corePoolSize 核心线程池大小
                poolSize,         //aximumPoolSize 最大线程池大小
                30,               //keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间
                TimeUnit.MINUTES, //TimeUnit keepAliveTime时间单位 这里是秒
                new LinkedBlockingQueue<Runnable>(),   //workQueue 阻塞队列
                new ZKEventThreadFactory());           //线程工厂
    }

    /**
     * 销毁线程池
     *
     * @return void
     */
    public void destory() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    /**
     * 处理事件
     *
     * @param zkEvent
     */
    public void submit(final ZKEvent zkEvent) {
        pool.submit(new Runnable() {
            @Override
            public void run() {
                int eventId = index.incrementAndGet();
                try {
                    LOG.debug("Handling event-" + eventId + " " + zkEvent);
                    zkEvent.run();
                } catch (Exception e) {
                    LOG.error("Error handling event [" + zkEvent + "]", e);
                }
                LOG.debug("Handled the event-" + eventId);
            }
        });
    }


    /**
     * 私有内部类,线程创建工厂
     */
    private class ZKEventThreadFactory implements ThreadFactory {
        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            String threadName = "ZkClient-EventThread-" + count.addAndGet(1);
            t.setName(threadName);
            return t;
        }
    }
}