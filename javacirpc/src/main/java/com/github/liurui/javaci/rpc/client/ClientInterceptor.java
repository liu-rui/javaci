package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;

import java.lang.reflect.InvocationHandler;

/**
 * Created by liurui on 17-3-1.
 */
public interface ClientInterceptor  extends InvocationHandler {
    void init(RpcConfig.ClientConfig clientConfig) throws Throwable;
}
