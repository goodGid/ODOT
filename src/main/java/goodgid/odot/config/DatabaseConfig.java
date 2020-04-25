/*
 * Copyright (c) 2020 LINE Corporation. All rights reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package goodgid.odot.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.ArrayUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan(
        basePackages = "goodgid.odot.repository.oltp",
        sqlSessionFactoryRef = "sqlSessionFactory")
@PropertySources({
        @PropertySource("classpath:META-INF/database/datasource.properties"),
        @PropertySource("classpath:META-INF/database/db.properties")
})
public class DatabaseConfig implements EnvironmentAware {

    private Environment env;

    @Bean(name = "dataSource", destroyMethod = "")
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(getProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(getProperty("spring.datasource.url"));
        dataSource.setUsername(getProperty("spring.datasource.username"));
        dataSource.setPassword(getProperty("spring.datasource.password"));
        dataSource.setDefaultAutoCommit(false);
        return dataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource,
                                                   ApplicationContext applicationContext) throws IOException {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(
                applicationContext.getResource("classpath:META-INF/mybatis/mybatis-config.xml"));
        factoryBean.setMapperLocations(
                applicationContext.getResources("classpath:META-INF/mybatis/mapper/*.xml"));
        factoryBean.setTypeAliasesPackage("goodgid.odot.model");

        Interceptor plugin = (Interceptor) applicationContext.getBean("sqlQueryLogInterceptor");
        factoryBean.setPlugins((Interceptor[]) ArrayUtils.add(null, plugin));

        return factoryBean;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Override
    public void setEnvironment(Environment environment) {
        // Initialize `env value by implementing EnvironmentAware
        this.env = environment;
    }

    private String getProperty(String name) {
        return env.getProperty(name);
    }
}
