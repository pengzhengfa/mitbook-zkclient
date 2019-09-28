package com.mitbook.zkclient.exception;

import org.apache.zookeeper.KeeperException;

/**
 * @author pengzhengfa
 */
public class ZKNoNodeException extends ZKException {

    private static final long serialVersionUID = 1L;

    public ZKNoNodeException() {
        super();
    }

    public ZKNoNodeException(KeeperException cause) {
        super(cause);
    }

    public ZKNoNodeException(String message, KeeperException cause) {
        super(message, cause);
    }

    public ZKNoNodeException(String message) {
        super(message);
    }
}
