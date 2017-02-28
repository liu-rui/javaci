package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;
import com.google.common.base.Stopwatch;
import com.google.common.net.HostAndPort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.transport.TSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by liurui on 17-2-28.
 */
@Component()
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultRpcClientPool implements RpcClientPool, RpcClientContainer {
    private static final Log logger = LogFactory.getLog(DefaultRpcClientPool.class);
    private static final int MaxConnectionCount = 10;
    private static final int GetTimeout = 30 * 1000;
    private final Stack<RpcClient> _idleRpcClients = new Stack<>();
    private final List<RpcClient> _usingRpcClients = new ArrayList<>();
    private RpcConfig.ClientConfig clientConfig;
    private ServiceFinder serviceFinder;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void init(RpcConfig.ClientConfig clientConfig) throws OperationNotSupportedException {
        this.clientConfig = clientConfig;

        if (clientConfig.getDirect() != null)
            serviceFinder = applicationContext.getBean("direct", ServiceFinder.class);
        else if (clientConfig.getServiceCentre() != null)
            serviceFinder = applicationContext.getBean("serviceCentre", ServiceFinder.class);
        else
            throw new OperationNotSupportedException("");
    }

    public RpcClient Get() throws TimeoutException {
        int timeOut = GetTimeout;
        Stopwatch watch = Stopwatch.createStarted();

        while (timeOut > 0) {
            RpcClient result = GetByPool();

            if (result != null)
                return result;

            try {
                this.wait(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeOut = timeOut - (int) watch.elapsed(TimeUnit.MILLISECONDS);
            watch.reset();
            watch.start();
        }

        throw new TimeoutException(String.format("没有可用的RPC连接，服务：%s", clientConfig.getContract()));
    }

    private RpcClient GetByPool() {
        synchronized (this) {
            RpcClient ret = getByIdle();

            if (MaxConnectionCount == _usingRpcClients.size())
                return null;

            if (ret == null)
                ret = createRpcClient();

            if (ret != null)
                _usingRpcClients.add(ret);
            logger.trace(String.format("RPC接口类型[%s],正在使用的连接数为%s",
                    clientConfig.getContract(),
                    _usingRpcClients.size()));
            return ret;
        }
    }


    private RpcClient getByIdle() {
        while (true) {
            RpcClient ret = null;
            try {
                ret = _idleRpcClients.pop();
            } catch (EmptyStackException ex) {
            }

            if (ret == null) return null;

            if (!ret.isOpen()) {
                ret.relase();
            } else
                return ret;
        }
    }

    private RpcClient createRpcClient() {
        int count = 3;
        int i = 0;

        while (i < count) {
            HostAndPort address;
            try {
                address = serviceFinder.getServiceLocation();
            } catch (Exception e) {
                logger.error(String.format("RPC接口类型[%s],获取可用的rpc连接对象时出现错误", clientConfig.getContract()), e);
                return null;
            }

            TSocket socket = new TSocket(address.getHost(), address.getPort(), 20000);

            try {
                socket.open();
                return new RpcClient(this, socket, clientConfig.getContract());
            } catch (Exception e) {
                logger.error(String.format("RPC接口类型[%s],rpc尝试连接时出现异常,rpc服务器地址为%s",
                        clientConfig.getContract(),
                        address), e);
                serviceFinder.reportInvalidServiceLocation(address);
            }
            i++;
        }

        return null;
    }


    public void release(RpcClient clientSocket) {
        synchronized (this) {
            _usingRpcClients.remove(clientSocket);

            if (clientSocket.isOpen())
                _idleRpcClients.push(clientSocket);
        }
        this.notifyAll();
    }
}
