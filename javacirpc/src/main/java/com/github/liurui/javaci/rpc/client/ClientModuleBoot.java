package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.core.modulebundle.ModuleBoot;
import com.github.liurui.javaci.rpc.RpcConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Created by liurui on 17-3-1.
 */
@Component
public class ClientModuleBoot implements ModuleBoot {
    private static final Log logger = LogFactory.getLog(DefaultRpcClientPool.class);
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private RpcConfig rpcConfig;

    @Override
    public void execute() {
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
            logger.trace(String.format("注册rpc客户端代理,接口为%s", clientConfig.getContract()));
        });
    }
}
