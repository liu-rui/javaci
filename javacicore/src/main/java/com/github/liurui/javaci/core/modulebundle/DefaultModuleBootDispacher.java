package com.github.liurui.javaci.core.modulebundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by liurui on 17-3-1.
 */
@Component
public class DefaultModuleBootDispacher implements ModuleBootDispacher {
    private static final Logger logger = LogManager.getLogger(DefaultModuleBootDispacher.class);


    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void execute() {
        Map<String, ModuleBoot> beans = applicationContext.getBeansOfType(ModuleBoot.class);

        logger.trace(String.format("自动加载模块单元，共发现【%s】个", beans.size()));

        for (Map.Entry<String, ModuleBoot> entry : beans.entrySet()) {
            logger.trace(String.format("加载模块,类型:%s", entry.getValue().getClass().getName()));
            entry.getValue().execute();
        }
    }
}
