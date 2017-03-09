package com.github.liurui.javaci.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liurui on 17-3-9.
 */
public final class Guard {
    private static final Logger logger = LoggerFactory.getLogger(Guard.class);

    public static <T> T tryDo(int tryCount, int interval, Action<T> action) throws Throwable {
        logger.info("执行重试，共{}机会", tryCount);
        T ret = null;
        for (int i = 0; i < tryCount; i++) {
            logger.info("第{}次机会", i + 1);
            try {
                ret = action.execute();
                break;
            } catch (Exception e) {
                logger.info("发生异常 {}" , e.toString());
                if (i == tryCount - 1)
                    throw e;
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
            }
        }
        return ret;
    }


}
