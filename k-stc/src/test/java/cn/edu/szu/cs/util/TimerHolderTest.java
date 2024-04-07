package cn.edu.szu.cs.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TimerHolderTest {
    private static final Log logger = LogFactory.get();

    @Test
    void test() {

        String id = TimerHolder.start();

        logger.info("id: {}", id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long timestamp = TimerHolder.stop();

        Assert.isTrue(timestamp>=1000&&timestamp<=1100);

    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme