package com.github.liurui.javaci.rpc.client;

import com.github.liurui.javaci.rpc.RpcConfig;
import com.github.liurui.javaci.rpc.servicecentre.FindService;
import com.google.common.net.HostAndPort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.naming.OperationNotSupportedException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by liurui on 17-2-28.
 */
@Component("serviceCentre")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServiceCentreServiceFinder implements ServiceFinder {
    private static final Log logger = LogFactory.getLog(ServiceCentreServiceFinder.class);
    private RpcConfig.ClientConfig clientConfig;
    private final List<String> services = new ArrayList<>();
    private int index = -1;
    private final Random random = new Random(LocalTime.now().getNano());

    @Autowired
    private FindService findService;

    @Override
    public void init(RpcConfig.ClientConfig clientConfig) {
        this.clientConfig = clientConfig;

        findService.init(clientConfig.getServiceCentre().getRespositoryServer(),
                clientConfig.getServiceCentre().getName(),
                (data) -> {
                    synchronized (this) {
                        services.clear();
                        services.addAll(data);
                    }
                });
    }

    @Override
    public HostAndPort getServiceLocation() throws OperationNotSupportedException {
        HostAndPort serviceAddress;

        synchronized (this) {
            if (services.isEmpty())
                throw new OperationNotSupportedException(String.format("目前在仓库%s上，没有针对%s的可用服务",
                        clientConfig.getServiceCentre().getRespositoryServer(),
                        clientConfig.getServiceCentre().getName()));


            if (index == -1)
                index = services.size() == 1 ? 0 : random.nextInt(services.size());
            else if (index > services.size() - 1)
                index = 0;

            serviceAddress = HostAndPort.fromString(services.get(index));
            index += 1;
        }
        return serviceAddress;
    }

    @Override
    public void reportInvalidServiceLocation(HostAndPort serviceLocation) {
        synchronized (this) {
            services.remove(serviceLocation.toString());
        }
    }
}
