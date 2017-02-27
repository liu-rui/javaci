package com.github.liurui.javaci.rpc.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Created by liurui on 17-2-27.
 */
@Component
@PropertySource("classpath:Cicada.properties")
public class ServerConfiguration {
    @Value("${Cicada.Rpc.Server.Port}")
    public int port;
    @Value("${Cicada.Rpc.Server.ServiceCentre.RespositoryServer}")
    public String publishRespositoryServer;
    @Value("${Cicada.Rpc.Server.ServiceCentre.Name}")
    public String publishName;
    @Value("${Cicada.Rpc.Server.ServiceCentre.Server}")
    public String publishServer;
}
