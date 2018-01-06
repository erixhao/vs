package com.vs.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtilsTest {

    @Test
    public void fromString() {
        LocalDate date = LocalDate.now();

        String now = DateUtils.toString(date);
        System.out.println(now);

        LocalDate nowDate = DateUtils.fromString(now);
        System.out.println(nowDate);
        Assert.assertEquals(date, nowDate);
    }

    @Test
    public void testDuration() throws InterruptedException {
        LocalDateTime begin = LocalDateTime.now();
        Thread.sleep(1000);

        System.out.println(Duration.between(begin, LocalDateTime.now()).toMillis());
    }
}