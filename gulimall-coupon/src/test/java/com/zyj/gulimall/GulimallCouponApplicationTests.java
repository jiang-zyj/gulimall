package com.zyj.gulimall;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        //LocalDate now = LocalDate.now();
        //LocalDate plusDays = now.plusDays(1);
        //LocalDate plusDays2 = now.plusDays(2);
        //
        //System.out.println(now);
        //System.out.println(plusDays);
        //System.out.println(plusDays2);
        //
        //LocalTime min = LocalTime.MIN;
        //LocalTime max = LocalTime.MAX;
        //System.out.println(min);
        //System.out.println(max);
        //
        //LocalDateTime start = LocalDateTime.of(now, min);
        //LocalDateTime end = LocalDateTime.of(plusDays2, max);
        //System.out.println(start);
        //System.out.println(end);
        LocalDate now = LocalDate.now();
        LocalDate plusDays = now.plusDays(2);
        LocalDateTime end = LocalDateTime.of(plusDays, LocalTime.MAX);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format);
    }

}
