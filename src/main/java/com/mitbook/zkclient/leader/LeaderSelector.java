package com.mitbook.zkclient.leader;

import java.util.List;

/**
 * @author pengzhengfa
 */
public interface LeaderSelector {
    /**
     * 启动参与选举Leader
     *
     * @return void
     */
    void start();

    /**
     * 重新添加当前线程到选举队列
     *
     * @return void
     */
    void requeue();

    /**
     * 获得Leader ID
     *
     * @return String
     */
    String getLeader();

    /**
     * 是否是Leader
     *
     * @return boolean
     */
    boolean isLeader();

    /**
     * 获得当前的所有参与者的路径名
     *
     * @return List<String>
     */
    List<String> getParticipantNodes();


    /**
     * 终止等待成为Leader
     *
     * @return void
     */
    void interruptLeadership();

    /**
     * 关闭Leader选举
     *
     * @return void
     */
    void close();

}
