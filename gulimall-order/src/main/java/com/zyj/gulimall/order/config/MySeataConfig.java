package com.zyj.gulimall.order.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;


/**
 * @program: gulimall
 * @ClassName MySeataConfig
 * @author: YaJun
 * @Date: 2022 - 01 - 17 - 22:24
 * @Package: com.zyj.gulimall.order.config
 * @Description: 使用Seata DataSourceProxy 代理自己的数据源
 */
public class MySeataConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        // properties.initializeDataSourceBuilder().type(type).build();
        // 创建数据源
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        // 包装为seata的数据源代理
        return new DataSourceProxy(dataSource);
    }

}
