package com.github.liurui.javaci.rpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "cicada.rpc")
public class RpcConfig {
    public static class ServerConfig {
        private int port;
        private ServiceCentre serviceCentre;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public ServiceCentre getServiceCentre() {
            return serviceCentre;
        }

        public void setServiceCentre(ServiceCentre serviceCentre) {
            this.serviceCentre = serviceCentre;
        }

        @Override
        public String toString() {
            return "ServerConfig{" +
                    "port=" + port +
                    ", serviceCentre=" + serviceCentre +
                    '}';
        }
    }

    public static class ServiceCentre {
        private String respositoryServer;
        private String name;
        private String server;

        public String getRespositoryServer() {
            return respositoryServer;
        }

        public void setRespositoryServer(String respositoryServer) {
            this.respositoryServer = respositoryServer;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        @Override
        public String toString() {
            return "ServiceCentre{" +
                    "respositoryServer='" + respositoryServer + '\'' +
                    ", name='" + name + '\'' +
                    ", server='" + server + '\'' +
                    '}';
        }
    }

    public static class ClientConfig {
        private String contract;
        private ClientDirectConfig direct;
        private ServiceCentre serviceCentre;

        public String getContract() {
            return contract;
        }

        public void setContract(String contract) {
            this.contract = contract;
        }

        public ClientDirectConfig getDirect() {
            return direct;
        }

        public void setDirect(ClientDirectConfig direct) {
            this.direct = direct;
        }

        public ServiceCentre getServiceCentre() {
            return serviceCentre;
        }

        public void setServiceCentre(ServiceCentre serviceCentre) {
            this.serviceCentre = serviceCentre;
        }

        @Override
        public String toString() {
            return "ClientConfig{" +
                    "contract='" + contract + '\'' +
                    ", direct=" + direct +
                    ", serviceCentre=" + serviceCentre +
                    '}';
        }
    }

    public static class ClientDirectConfig {
        private String server;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        @Override
        public String toString() {
            return "ClientDirectConfig{" +
                    "server='" + server + '\'' +
                    '}';
        }
    }


    private ServerConfig server;
    private List<ClientConfig> clients = new ArrayList<>();

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public List<ClientConfig> getClients() {
        return clients;
    }

    @Override
    public String toString() {
        return "RpcConfig{" +
                "server=" + server +
                ", clients=" + clients +
                '}';
    }
}
