package com.github.liurui.javaci.rpc.servicecentre;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by liurui on 17-2-28.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ZkFinder implements Finder {
    private static final Log logger = LogFactory.getLog(DefaultServicePublisher.class);
    private static final int RepairInterval = 2 * 60 * 1000;
    private static final int Timeout = 10 * 1000;
    private final ConcurrentHashMap<String, ServiceListChanged> actions = new ConcurrentHashMap<String, ServiceListChanged>();
    private ZooKeeper zooKeeper;
    private String respository;
    private final Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            logger.trace(String.format("ZooKeeper 状态发生更改 RPC路径：%s 服务中心地址：%s event.type:%s event.state:%s",
                    event.getPath(),
                    respository,
                    event.getType(),
                    event.getState()));

            switch (event.getState()) {
                case Disconnected:
                case Expired:
                case Unknown:
                case NoSyncConnected:
                    StartRepair();
                    return;
            }

            try {
                ServiceListChanged action = actions.get(event.getPath());

                getData(event.getPath(), action);
            } catch (Exception ex) {
                logException(ex);
                StartRepair();
            }
        }
    };

    @Override
    public void init(String respository) {

        this.respository = respository;
        try {
            if (zooKeeper == null)
                Create();
        } catch (Exception ex) {
            logException(ex);
            StartRepair();
        }
    }

    @Override
    public void add(String path, ServiceListChanged action) {
        if (StringUtils.isEmpty(path))
            throw new IllegalArgumentException("path");

        if (action == null)
            throw new IllegalArgumentException("action");

        if (actions.containsKey(path))
            throw new IllegalArgumentException(String.format("您的配置文件中存在重复的Rpc路径，路径为:%s", path));
        actions.put(path, action);

        try {
            if (zooKeeper != null && zooKeeper.getState().equals(ZooKeeper.States.CONNECTED))
                getData(path, action);
        } catch (Exception ex) {
            logException(ex);
            StartRepair();
        }
    }


    private void StartRepair() {
        Thread thread = new Thread(() ->
        {
            logger.trace(String.format("RPC服务中心%s断开连接，尝试连接", respository));

            while (true) {
                try {
                    repairProcess();
                    break;
                } catch (Exception e) {
                    Close();
                    try {
                        Thread.sleep(RepairInterval);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            logger.trace(String.format("已与RPC服务中心%s建立连接", respository));
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void getData(String path, ServiceListChanged action) throws KeeperException, InterruptedException {
        List<String> data = zooKeeper.getChildren(path, watcher);
        if (data == null) data = new ArrayList<>();

        logger.trace(String.format("RPC路径%s发现有新的服务器列表,服务器列表为：%s", path, String.join(",", data)));

        if (!data.isEmpty())
            action.on(data);
    }

    private void getDataList() throws KeeperException, InterruptedException {
        for (ConcurrentHashMap.Entry<String, ServiceListChanged> item : actions.entrySet()) {
            getData(item.getKey(), item.getValue());
        }

        actions.forEach((path, action) -> {
            try {
                getData(path, action);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    private void repairProcess() {
        if (zooKeeper != null && !zooKeeper.getState().isAlive())
            Close();

        try {
            if (zooKeeper == null)
                Create();

            getDataList();
        } catch (Exception ex) {
            String message = logException(ex);
            throw new Error(message, ex);
        }
    }


    private String logException(Exception ex) {
        String message;

        if (ex instanceof KeeperException.ConnectionLossException)
            message = String.format("无法连接到服务中心，地址为:%s", respository);
        else if (ex instanceof KeeperException.SessionExpiredException)
            message = String.format("连接服务中心时发生超时，zookeeper地址为:%s", respository);
        else
            message = String.format("zookeeper获取节点数据出现异常，zookeeper地址为:%s ", respository);

        logger.trace(message + ex.toString());
        return message;
    }


    private void Create() throws IOException, TimeoutException {
        zooKeeper = new ZooKeeper(respository, Timeout, null);
        int max = 10;

        while (!zooKeeper.getState().equals(ZooKeeper.States.CONNECTED) && max-- > 1) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!zooKeeper.getState().equals(ZooKeeper.States.CONNECTED))
                throw new TimeoutException(String.format("连接服务中心时发生超时，zookeeper地址为:%s", respository));
    }

    private void Close() {
        if (zooKeeper == null) return;

        try {
            zooKeeper.close();
            zooKeeper = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
