package com.github.liurui.javaci.rpc.servicecentre;

/**
 * Created by liurui on 17-2-28.
 */
public interface Finder {
    void init(String respository);

    void add(String path, ServiceListChanged action);
}
