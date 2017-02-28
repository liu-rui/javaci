package com.github.liurui.javaci.rpc.servicecentre;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

/**
 * Created by liurui on 17-2-27.
 */
@Component
public class DefaultServicePublisher implements ServicePublisher {
    private static final Log logger = LogFactory.getLog(DefaultServicePublisher.class);
    private static final long RepairInterval = 10 * 1000;
    private static final int Timeout = 10 * 1000;
    private ZooKeeper zooKeeper;
    private String respository;
    private String directory;
    private String dataPath;
    private boolean running;
    private final Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            logger.trace(String.format("ZooKeeper 状态发生更改 服务中心地址：%s event.type:%s event.state:%s", respository, event.getType(), event.getState()));

            switch (event.getState()) {
                case Disconnected:
                case Expired:
                case Unknown:
                case NoSyncConnected:
                    startRepair();
                    return;
            }

            try {
                validateExistPath();
            } catch (Exception ex) {
                logException(ex);
                startRepair();
            }
        }
    };

    public void init(String respository, String path, String data) {
        this.respository = respository;
        if (StringUtils.isEmpty(path) || path.charAt(0) != '/')
            directory = '/' + path;
        else
            directory = path;

        dataPath = directory.charAt(directory.length() - 1) == '/' ? directory.concat(data) : directory.concat("/").concat(data);
    }

    public void publish() {
        running = true;
        startRepair();
    }

    public void cancel() {
        running = false;
    }


    private void startRepair() {
        Thread thread = new Thread(() -> {
            logger.trace(String.format("RPC服务中心%s断开连接，尝试连接", respository));
            while (running) {
                try {
                    repairProcess();
                    break;
                } catch (Exception ex) {
                    close();
                    try {
                        Thread.sleep(RepairInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            logger.trace(String.format("已与RPC服务中心%s建立连接", respository));
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void repairProcess() throws ConnectException {
        if (zooKeeper != null && !zooKeeper.getState().isAlive()) {
            close();
            try {
                Thread.sleep(Duration.ofMinutes(2).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            if (zooKeeper == null)
                create();

            mkdirs(directory);
            zooKeeper.create(dataPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            validateExistPath();
        } catch (Exception ex) {
            String message = logException(ex);
            throw new ConnectException(message);
        }
    }

    private void mkdirs(String path) throws KeeperException, InterruptedException {
        int pos = 1; // skip first slash, root is guaranteed to exist

        do {
            pos = path.indexOf('/', pos + 1);

            if (pos == -1) {
                pos = path.length();
            }

            String subPath = path.substring(0, pos);
            if (zooKeeper.exists(subPath, false) == null) {
                try {
                    zooKeeper.create(subPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                } catch (KeeperException.NodeExistsException e) {
                    // ignore... someone else has created it since we checked
                }
            }
        }
        while (pos < path.length());
    }


    private void validateExistPath() throws KeeperException, InterruptedException, ConnectException {
        if (zooKeeper.exists(dataPath, watcher) == null)
            throw new ConnectException("节点不存在");
    }

    private String logException(Exception ex) {
        String message;

        if (ex instanceof KeeperException.ConnectionLossException)
            message = String.format("无法连接到服务中心，地址为:%s", respository);
        else if (ex instanceof KeeperException.SessionExpiredException)
            message = String.format("连接服务中心时发生超时，地址为:%s", respository);
        else
            message = String.format("RPC zookeeper注册节点时出现异常，地址为:%s ", respository);

        logger.trace(message + ex.toString());
        return message;
    }

    private void create() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(respository, Timeout, null);
        int max = 10;

        while (!zooKeeper.getState().equals(ZooKeeper.States.CONNECTED) && max-- > 1) {
            Thread.sleep(1000);
        }
    }

    private void close() {
        if (zooKeeper == null)
            return;

        try {
            zooKeeper.close();
            zooKeeper = null;
        } catch (Exception ex) {
        }
    }
}
