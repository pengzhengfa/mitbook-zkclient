package com.mitbook.zkclient.event;

/**
 * @author pengzhengfa
 */
public abstract class ZKEvent {
    private String description;

    public ZKEvent(String description) {
        this.description = description;
    }

    public abstract void run() throws Exception;

    @Override
    public String toString() {
        return "ZKEvent[" + description + "]";
    }
}
