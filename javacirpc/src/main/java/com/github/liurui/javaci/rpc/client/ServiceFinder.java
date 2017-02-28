package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;
import com.google.common.net.HostAndPort;

import javax.naming.OperationNotSupportedException;

/**
 * Created by liurui on 17-2-28.
 */
public interface ServiceFinder {
    void init(RpcConfig.ClientConfig clientConfig);
    HostAndPort getServiceLocation() throws OperationNotSupportedException;
    void reportInvalidServiceLocation(HostAndPort serviceLocation);
}
