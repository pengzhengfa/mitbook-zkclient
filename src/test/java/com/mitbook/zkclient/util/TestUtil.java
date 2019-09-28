package com.mitbook.zkclient.util;

import com.mitbook.zkclient.exception.ZKException;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 测试工具类
 * @author pengzhengfa
 */
public class TestUtil {

    /**
     * 等待直到callable返回值等于期望值expectedValue,或者直到超时.
     *
     * @param expectedValue
     * @param callable
     * @param timeUnit
     * @param timeout
     * @return T 返回回调函数callable的返回值
     * @throws Exception
     */
    public static <T> T waitUntil(T expectedValue, Callable<T> callable, TimeUnit timeUnit, long timeout) throws Exception {
        long startTime = System.currentTimeMillis();
        do {
            T actual = callable.call();
            if (expectedValue.equals(actual)) {
                System.out.println("TestUtil.waitUntil expected");
                return actual;
            }
            if (System.currentTimeMillis() > startTime + timeUnit.toMillis(timeout)) {
                System.out.println("TestUtil.waitUntil timeout!");
                return actual;
            }
            Thread.sleep(300);
        } while (true);
    }

    /**
     * 启动一个单实例的ZooKeeper server
     *
     * @param serverName
     * @param port
     * @return ZKServer
     */
    public static ZKServer startServer(String serverName, int port) {
        String dataPath = "./target/test-classes/" + serverName + "/data";
        String logPath = "./target/test-classes/" + serverName + "/log";
        try {
            FileUtils.deleteDirectory(new File(dataPath));
            FileUtils.deleteDirectory(new File(logPath));
        } catch (IOException e) {
            throw new ZKException("start server error!", e);
        }
        return startServer(dataPath, logPath, port);
    }

    public static ZKServer startZkServer(TemporaryFolder temporaryFolder, int port) throws IOException {
        File dataFolder = temporaryFolder.newFolder("data");
        File logFolder = temporaryFolder.newFolder("log");
        return startServer(dataFolder.getAbsolutePath(), logFolder.getAbsolutePath(), port);
    }

    private static ZKServer startServer(String dataPath, String logPath, int port) {
        ZKServer zkServer = new ZKServer(dataPath, logPath, port, ZKServer.DEFAULT_TICK_TIME, 100);
        zkServer.start();
        return zkServer;
    }
}
