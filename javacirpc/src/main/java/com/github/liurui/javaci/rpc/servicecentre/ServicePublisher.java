package com.github.liurui.javaci.rpc.servicecentre;

/**
 * Created by liurui on 17-2-27.
 */
public interface ServicePublisher {
    void init(String respository, String path, String data);

    void publish();

    void cancel();
}
