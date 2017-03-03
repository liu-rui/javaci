package com.github.liurui.javaci.rpc.autoconfiguration;

import com.github.liurui.javaci.rpc.RpcConfig;
import com.github.liurui.javaci.rpc.client.ClientInterceptor;
import com.github.liurui.javaci.rpc.client.DefaultRpcClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Created by liurui on 17-3-2.
 */

@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcManualTypeConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcClientPool.class);


    public RpcManualTypeConfiguration(ConfigurableApplicationContext applicationContext,
                                      RpcConfig rpcConfig) {
        List<RpcConfig.ClientConfig> clientConfigs = rpcConfig.getClients();

        if (clientConfigs.isEmpty()) return;

        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        clientConfigs.forEach(clientConfig -> {
            ClientInterceptor clientInterceptor = beanFactory.getBean(ClientInterceptor.class);

            try {
                clientInterceptor.init(clientConfig);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            Class face = null;
            try {
                face = Class.forName(clientConfig.getContract());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Object proxy = Proxy.newProxyInstance(face.getClassLoader(), new Class<?>[]{face}, clientInterceptor);

            beanFactory.registerSingleton(clientConfig.getContract(), proxy);
            logger.trace("注册rpc客户端代理,接口为{}", clientConfig.getContract());
        });
    }
}
