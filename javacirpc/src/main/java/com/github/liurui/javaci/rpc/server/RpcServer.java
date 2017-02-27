package com.github.liurui.javaci.rpc.server;

/**
 * Created by liurui on 17-2-27.
 */
public interface RpcServer {
    <T> void run(Class<T> action);

    void close();
}
