package com.github.liurui.javaci.rpc.servicecentre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liurui on 17-2-28.
 */
@Component
public class DefaultFindService implements FindService {
    private final Map<String, Finder> finders = new HashMap<>();
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void init(String respository, String path, ServiceListChanged action) {
        if (StringUtils.isEmpty(respository)) throw new IllegalArgumentException("respository");
        if (StringUtils.isEmpty(path)) throw new IllegalArgumentException("path");
        if (action == null) throw new IllegalArgumentException("action");

        Finder finder;

        if (!finders.containsKey(respository)) {
            finder = applicationContext.getBean(Finder.class);
            finder.init(respository);
            finders.put(respository, finder);
        } else
            finder = finders.get(respository);

        finder.add(path, action);
    }
}
