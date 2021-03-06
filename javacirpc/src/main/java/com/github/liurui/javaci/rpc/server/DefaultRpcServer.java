package com.github.liurui.javaci.rpc.server;


import com.github.liurui.javaci.rpc.RpcConfig;
import com.github.liurui.javaci.rpc.servicecentre.ServicePublisher;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by liurui on 17-2-27.
 */
@Component
public class DefaultRpcServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcServer.class);
    private static final int clientTimeoutDefault = 5 * 60 * 1000;
    private boolean _published = false;

    private TServer server;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RpcConfig rpcConfig;

    @Autowired
    private ServicePublisher servicePublisher;

    @Override
    public <T> void run(Class<T> action) {
        Thread publishThread = new Thread(this::publish);

        publishThread.setDaemon(true);
        publishThread.start();


        try {
            runPrivate(action);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
//        Thread rpcServerThread = new Thread(() -> {
//            try {
//                runPrivate(action);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        rpcServerThread.setDaemon(true);
//        rpcServerThread.start();
    }

    public <T> void runPrivate(Class<T> action) throws ClassNotFoundException, NoSuchMethodException, TTransportException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Class ifaceClass = Class.forName(action.getTypeName());
        Class processorClass = Class.forName(action.getTypeName().substring(0,action.getTypeName().lastIndexOf('$')) + "$Processor");
        Constructor constructor = processorClass.getConstructor(ifaceClass);
        Object faceImpl = applicationContext.getBean(ifaceClass);
        TProcessor processor = (TProcessor) constructor.newInstance(faceImpl);
        TServerSocket serverTransport = new TServerSocket(rpcConfig.getServer().getPort(), clientTimeoutDefault);
        TCompactProtocol.Factory protFactory = new TCompactProtocol.Factory();
        TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
        args.processor(processor);
        args.protocolFactory(protFactory);
        server = new TThreadPoolServer(args);
        logger.info("启动Rpc服务器 端口：{}", rpcConfig.getServer().getPort());
        try {
            server.serve();
        } finally {
            logger.info("Rpc服务器已停止运行");
        }
    }

    private void publish() {
        if (rpcConfig.getServer().getServiceCentre()  == null ||
                StringUtils.isEmpty(rpcConfig.getServer().getServiceCentre().getRespositoryServer())) return;
        _published = true;
        servicePublisher.init(rpcConfig.getServer().getServiceCentre().getRespositoryServer(),
                rpcConfig.getServer().getServiceCentre().getName(),
                rpcConfig.getServer().getServiceCentre().getServer() + ":" + rpcConfig.getServer().getPort());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        servicePublisher.publish();
    }

    @Override
    public void close() {
        if (servicePublisher != null)
            servicePublisher.cancel();

        if (server != null)
            server.stop();
    }
}
