package com.mitbook.zkclient;

import com.mitbook.zkclient.lock.ZKDistributedLock;
import com.mitbook.zkclient.util.TestSystem;
import com.mitbook.zkclient.util.TestUtil;
import com.mitbook.zkclient.util.ZKServer;
import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ZKDistributedLockTest {

    private TestSystem testSystem = TestSystem.getInstance();
    private ZKServer zkServer = null;
    private ZKClient zkClient = null;

    @Before
    public void before() {
        zkServer = testSystem.getZKserver();
        zkClient = ZKClientBuilder.newZKClient()
                .servers("localhost:" + zkServer.getPort())
                .sessionTimeout(1000)
                .build();
    }

    @After
    public void after() {
        testSystem.cleanup(zkClient);
    }

    /**
     * 测试分布式锁
     *
     * @return void
     * @throws Exception
     */
    @Test
    public void testDistributedLock() throws Exception {
        final String lockPath = "/zk/lock";
        zkClient.createRecursive(lockPath, null, CreateMode.PERSISTENT);
        final AtomicInteger integer = new AtomicInteger(0);
        final List<String> msgList = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            Thread thread1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        ZKDistributedLock lock = ZKDistributedLock.newInstance(zkClient, lockPath);
                        lock.lock(0);
                        integer.getAndIncrement();
                        msgList.add("thread " + integer);
                        System.out.println("Thread " + integer + " got lock........");
                        System.out.println(lock.getParticipantNodes());
                        if (integer.get() == 3) {
                            Thread.sleep(1000);
                        }
                        if (integer.get() == 5) {
                            Thread.sleep(700);
                        }

                        if (integer.get() == 6 || integer.get() == 11) {
                            Thread.sleep(500);
                        }

                        if (integer.get() == 10) {
                            Thread.sleep(500);
                        }
                        if (integer.get() == 15) {
                            Thread.sleep(400);
                        }
                        lock.unlock();
                        System.out.println("thread " + integer + " unlock........");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {

                    }
                }
            });
            thread1.start();
        }

        //等待事件到达
        int size = TestUtil.waitUntil(20, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return msgList.size();
            }

        }, TimeUnit.SECONDS, 100);
        assertThat(size).isEqualTo(20);
        boolean flag = true;
        for (int i = 0; i < 20; i++) {
            if (!msgList.get(i).equals("thread " + (i + 1))) {
                flag = false;
            }
        }
        assertThat(flag).isTrue();
    }
}
