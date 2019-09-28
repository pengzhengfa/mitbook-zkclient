package com.mitbook.zkclient;

import com.mitbook.zkclient.leader.LeaderSelector;
import com.mitbook.zkclient.leader.ZKLeaderDelySelector;
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

public class ZKLeaderDelaySelectorTest {

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
    public void testZKLeaderDelaySeletor() throws Exception {
        final String leaderPath = "/zk/leader";
        final List<String> msgList = new ArrayList<String>();
        final CountDownLatch latch = new CountDownLatch(5);
        final CountDownLatch latch1 = new CountDownLatch(5);
        zkClient.createRecursive(leaderPath, null, CreateMode.PERSISTENT);
        final AtomicInteger index = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            final String name = "server:" + index.get();

            Thread thread1 = new Thread(new Runnable() {
                public void run() {
                    final ZKClient zkClient1 = ZKClientBuilder.newZKClient()
                            .servers("localhost:" + zkServer.getPort())
                            .sessionTimeout(1000)
                            .build();
                    final LeaderSelector selector = new ZKLeaderDelySelector(name, true, 3000, zkClient1, leaderPath, new ZKLeaderSelectorListener() {

                        @Override
                        public void takeLeadership(ZKClient client, LeaderSelector selector) {
                            msgList.add(name + " I am the leader");
                            System.out.println(name + ": I am the leader-" + selector.getLeader());
                            System.out.println(selector.getParticipantNodes());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            selector.close();
                            latch1.countDown();
                        }
                    });

                    try {
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
        int size = TestUtil.waitUntil(5, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return msgList.size();
            }

        }, TimeUnit.SECONDS, 100);

        assertThat(size).isEqualTo(5);
    }


    @Test
    public void testZKLeaderDelaySeletor1() throws Exception {
        final String leaderPath = "/zk/leader";
        final List<String> msgList = new ArrayList<String>();
        zkClient.createRecursive(leaderPath, null, CreateMode.PERSISTENT);

        final LeaderSelector selector = new ZKLeaderDelySelector("server1", true, 3000, zkClient, leaderPath, new ZKLeaderSelectorListener() {

            @Override
            public void takeLeadership(ZKClient client, LeaderSelector selector) {
                msgList.add("server1 I am the leader");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("server1: I am the leader-" + selector.getLeader());
                zkClient.reconnect();
            }
        });


        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                final ZKClient zkClient1 = ZKClientBuilder.newZKClient()
                        .servers("localhost:" + zkServer.getPort())
                        .sessionTimeout(1000)
                        .build();
                final LeaderSelector selector = new ZKLeaderDelySelector("server2", true, 3000, zkClient1, leaderPath, new ZKLeaderSelectorListener() {

                    @Override
                    public void takeLeadership(ZKClient client, LeaderSelector selector) {
                        msgList.add("server2 I am the leader");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("server2: I am the leader-" + selector.getLeader());
                        selector.close();
                    }
                });
                selector.start();
            }
        });

        selector.start();
        thread1.start();


        //等待事件到达
        int size = TestUtil.waitUntil(1, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return msgList.size();
            }

        }, TimeUnit.SECONDS, 100);

        assertThat(size).isEqualTo(1);
    }

    @Test
    public void testZKLeaderDelaySeletor2() throws Exception {
        final String leaderPath = "/zk/leader";
        final List<String> msgList = new ArrayList<String>();
        zkClient.createRecursive(leaderPath, null, CreateMode.PERSISTENT);

        final LeaderSelector selector = new ZKLeaderDelySelector("server1", true, 1, zkClient, leaderPath, new ZKLeaderSelectorListener() {

            @Override
            public void takeLeadership(ZKClient client, LeaderSelector selector) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                msgList.add("server1 I am the leader");
                System.out.println("server1: I am the leader-" + selector.getLeader());
                zkClient.reconnect();
            }
        });


        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                final ZKClient zkClient1 = ZKClientBuilder.newZKClient()
                        .servers("localhost:" + zkServer.getPort())
                        .sessionTimeout(1000)
                        .build();
                final LeaderSelector selector = new ZKLeaderDelySelector("server2", true, 1, zkClient1, leaderPath, new ZKLeaderSelectorListener() {

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
