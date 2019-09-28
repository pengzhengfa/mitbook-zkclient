package com.mitbook.zkclient.leader;

import com.mitbook.zkclient.ZKClient;

/**
 * @author pengzhengfa
 */
public interface ZKLeaderSelectorListener {
    void takeLeadership(ZKClient client, LeaderSelector selector);
}
