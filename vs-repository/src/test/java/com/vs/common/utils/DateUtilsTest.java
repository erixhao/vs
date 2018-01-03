package com.vs.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

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
}