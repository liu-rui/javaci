package com.github.liurui.javaci.rpc.servicecentre;

import java.util.List;

/**
 * Created by liurui on 17-2-28.
 */
@FunctionalInterface
public interface ServiceListChanged {
    void on(List<String>   services);
}
