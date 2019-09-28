package com.mitbook.zkclient;

import com.mitbook.zkclient.leader.LeaderSelector;
import com.mitbook.zkclient.leader.ZKLeaderSelector;
import com.mitbook.zkclient.leader.ZKLeaderSelectorListener;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ZKLeaderSelectorTest {

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
    public void testZKLeaderSeletor() throws Exception {
        final String leaderPath = "/zk/leader";
        final List<String> msgList = new ArrayList<String>();
        final CountDownLatch latch = new CountDownLatch(20);
        final CountDownLatch latch1 = new CountDownLatch(20);
        zkClient.createRecursive(leaderPath, null, CreateMode.PERSISTENT);
        final AtomicInteger index = new AtomicInteger(0);
        for (int i = 0; i < 20; i++) {
            final String name = "server:" + index.get();

            Thread thread1 = new Thread(new Runnable() {
                public void run() {
                    final ZKClient zkClient1 = ZKClientBuilder.newZKClient()
                            .servers("localhost:" + zkServer.getPort())
                            .sessionTimeout(1000)
                            .build();
                    final ZKLeaderSelector selector = new ZKLeaderSelector(name, true, zkClient1, leaderPath, new ZKLeaderSelectorListener() {

                        @Override
                        public void takeLeadership(ZKClient client, LeaderSelector selector) {
                            msgList.add(name + " I am the leader");
                            System.out.println(name + ": I am the leader-" + selector.getLeader());
                            selector.close();
                            latch1.countDown();
                        }
                    });

                    try {
                        System.out.println(name + ":waiting");
                        latch.await();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    selector.start();

                    try {
                        latch1.await();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread1.start();
            latch.countDown();
            index.getAndIncrement();
        }


        //等待事件到达
        int size = TestUtil.waitUntil(20, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return msgList.size();
            }

        }, TimeUnit.SECONDS, 100);

        assertThat(size).isEqualTo(20);
    }

    @Test
    public void testZKLeaderSeletor1() throws Exception {
        final String leaderPath = "/zk/leader";
        final List<String> msgList = new ArrayList<String>();
        zkClient.createRecursive(leaderPath, null, CreateMode.PERSISTENT);

        final LeaderSelector selector = new ZKLeaderSelector("server1", true, zkClient, leaderPath, new ZKLeaderSelectorListener() {

            @Override
            public void takeLeadership(ZKClient client, LeaderSelector selector) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                msgList.add("server1 I am the leader");
                System.out.println("server1: I am the leader-" + selector.getLeader());
            }
        });


        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                final ZKClient zkClient1 = ZKClientBuilder.newZKClient()
                        .servers("localhost:" + zkServer.getPort())
                        .sessionTimeout(1000)
                        .build();
                final LeaderSelector selector = new ZKLeaderSelector("server2", true, zkClient1, leaderPath, new ZKLeaderSelectorListener() {

                    @Override
                    public void takeLeadership(ZKClient client, LeaderSelector selector) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        msgList.add("server2 I am the leader");
                        System.out.println("server2: I am the leader-" + selector.getLeader());

                        selector.close();
                    }
                });
                selector.start();
            }
        });

        selector.start();
        thread1.start();
        Thread.sleep(1000);
        zkClient.reconnect();


        //等待事件到达
        int size = TestUtil.waitUntil(3, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return msgList.size();
            }

        }, TimeUnit.SECONDS, 100);
        System.out.println(msgList);
        assertThat(size).isEqualTo(3);
    }
}
