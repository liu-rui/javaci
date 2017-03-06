package com.github.liurui.javaci.rpc.client;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by liurui on 17-2-28.
 */
public class RpcClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcClientPool.class);
    private final TSocket socket;
    private final RpcClientContainer rpcClientContainer;
    private final Object client;

    public RpcClient(RpcClientContainer clientSocketContainer, TSocket socket, String contactType) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        Class<?> clientClass = Class.forName(contactType.substring(0,contactType.lastIndexOf('$')) + "$Client");
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
        socket.close();
    }

    @Override
    public void close() throws Exception {
        rpcClientContainer.release(this);
    }
}
