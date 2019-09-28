package com.mitbook.zkclient.exception;

import org.apache.zookeeper.KeeperException;

/**
 * @author pengzhengfa
 */
public class ZKTimeoutException extends ZKException {

    private static final long serialVersionUID = 1L;

    public ZKTimeoutException() {
        super();
    }

    public ZKTimeoutException(KeeperException cause) {
        super(cause);
    }

    public ZKTimeoutException(String message, KeeperException cause) {
        super(message, cause);
    }

    public ZKTimeoutException(String message) {
        super(message);
    }
}
