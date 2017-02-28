package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;

import javax.naming.OperationNotSupportedException;
import java.util.concurrent.TimeoutException;

/**
 * Created by liurui on 17-2-28.
 */
public interface RpcClientPool {

    void  init(RpcConfig.ClientConfig  clientConfig) throws OperationNotSupportedException;
    RpcClient Get() throws TimeoutException;
}
