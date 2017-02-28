package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;
import com.google.common.net.HostAndPort;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by liurui on 17-2-28.
 */
@Component("direct")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DirectServiceFinder implements ServiceFinder {
    private RpcConfig.ClientConfig clientConfig;
    private HostAndPort hostAndPort;

    @Override
    public void init(RpcConfig.ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        hostAndPort = HostAndPort.fromString(clientConfig.getDirect().getServer());
    }

    @Override
    public HostAndPort getServiceLocation() {
        return hostAndPort;
    }

    @Override
    public void reportInvalidServiceLocation(HostAndPort serviceLocation) {

    }
}
