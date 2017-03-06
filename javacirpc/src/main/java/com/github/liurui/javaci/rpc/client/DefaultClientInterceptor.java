package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;
import com.google.common.base.Stopwatch;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created by liurui on 17-3-1.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultClientInterceptor implements ClientInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcClientPool.class);

    @Autowired
    private RpcClientPool rpcClientPool;
    private RpcConfig.ClientConfig clientConfig;

    public void init(RpcConfig.ClientConfig clientConfig) throws Throwable {
        this.clientConfig = clientConfig;
        rpcClientPool.init(clientConfig);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object ret = null;

        try (RpcClient rpcClient = rpcClientPool.Get()) {
            try {
                ret = method.invoke(rpcClient.getClient(), args);
            }catch (InvocationTargetException e){
                Throwable cause = e.getCause();
                if(cause != null &&  cause instanceof TTransportException){
                    rpcClient.relase();
                }else{
                    throw  e;
                }
            }
        }
        finally {
            stopwatch.stop();
            logger.info("调用RPC方法{}.{}用时{}毫秒", clientConfig.getContract(),
                    method.getName(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
        return ret;
    }
}
