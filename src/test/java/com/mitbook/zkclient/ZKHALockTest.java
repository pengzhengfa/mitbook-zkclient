package com.mitbook.zkclient;

import com.mitbook.zkclient.lock.ZKHALock;
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

import static org.assertj.core.api.Assertions.assertThat;

public class ZKHALockTest {

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
     * 测试主从服务锁
     *
     * @return void
     * @throws Exception
     */
    @Test
    public void testZKHALock() throws Exception {
        final String lockPach = "/zk/halock";
        final List<String> msgList = new ArrayList<String>();
        zkClient.createRecursive(lockPach, null, CreateMode.PERSISTENT);


        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                final ZKClient zkClient1 = ZKClientBuilder.newZKClient()
                        .servers("localhost:" + zkServer.getPort())
                        .sessionTimeout(1000)
                        .build();
                ZKHALock lock = ZKHALock.newInstance(zkClient1, lockPach);
                //尝试获取锁,如果获取成功则变为主服务
                lock.lock();
                msgList.add("thread1 is master");
                System.out.println("thread1 now is master!");
                try {
                    Thread.sleep(1000 * 1);
                    zkClient1.reconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });

        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                final ZKClient zkClient2 = ZKClientBuilder.newZKClient()
                        .servers("localhost:" + zkServer.getPort())
                        .sessionTimeout(1000)
                        .build();
                ZKHALock lock = ZKHALock.newInstance(zkClient2, lockPach);
                //尝试获取锁,如果获取成功则变为主服务
                lock.lock();
                msgList.add("thread2 is master");
                System.out.println("thread2 now is master!");
                try {
                    Thread.sleep(1000 * 2);
                    lock.unlock();
                    zkClient2.unlistenAll();
                    zkClient2.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });

        Thread thread3 = new Thread(new Runnable() {
            public void run() {
                final ZKClient zkClient3 = ZKClientBuilder.newZKClient()
                        .servers("localhost:" + zkServer.getPort())
                        .sessionTimeout(1000)
                        .build();
                ZKHALock lock = ZKHALock.newInstance(zkClient3, lockPach);
                //尝试获取锁,如果获取成功则变为主服务
                lock.lock();
                msgList.add("thread3 is master");
                System.out.println("thread3 now is master!");
                try {
                    Thread.sleep(1000 * 3);
                    zkClient3.unlistenAll();
                    zkClient3.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        thread2.start();
        thread3.start();

        //等待事件到达
        int size = TestUtil.waitUntil(3, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return msgList.size();
            }

        }, TimeUnit.SECONDS, 100);
        assertThat(size).isEqualTo(3);


    }
}
