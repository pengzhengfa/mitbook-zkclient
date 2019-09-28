package com.mitbook.zkclient.exception;

import org.apache.zookeeper.KeeperException;

/**
 * @author pengzhengfa
 */
public class ZKNodeExistsException extends ZKException {
    private static final long serialVersionUID = 1L;

    public ZKNodeExistsException() {
        super();
    }

    public ZKNodeExistsException(KeeperException cause) {
        super(cause);
    }

    public ZKNodeExistsException(String message, KeeperException cause) {
        super(message, cause);
    }

    public ZKNodeExistsException(String message) {
        super(message);
    }
}
