package com.github.liurui.javaci.core.modulebundle;

/**
 * Created by liurui on 17-3-1.
 * 模块配置类；该接口的实现类会在系统启动的时候自动执行。
 */
public interface ModuleBoot {

    /**
     * 配置模块
     */
    void execute();
}
