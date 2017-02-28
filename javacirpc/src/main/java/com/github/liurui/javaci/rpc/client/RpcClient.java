package com.github.liurui.javaci.rpc.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by liurui on 17-2-28.
 */
public class RpcClient implements AutoCloseable {
    private static final Log logger = LogFactory.getLog(DefaultRpcClientPool.class);
    private final TSocket socket;
    private final RpcClientContainer rpcClientContainer;
    private final Object client;

    public RpcClient(RpcClientContainer clientSocketContainer, TSocket socket, String clientType) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        Class<?> clientClass = Class.forName(clientType);
        rpcClientContainer = clientSocketContainer;
        this.socket = socket;
        TCompactProtocol protocol = new TCompactProtocol(this.socket);
        Constructor constructor = clientClass.getConstructor(TProtocol.class);
        client = constructor.newInstance(protocol);
    }

    public Object getClient(){
        return client;
    }

    public boolean isOpen(){
        return socket != null && socket.isOpen();
    }

    public void relase(){
    }

    @Override
    public void close() throws Exception {
        rpcClientContainer.release(this);
    }
}