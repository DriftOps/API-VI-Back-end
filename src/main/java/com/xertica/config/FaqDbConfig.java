package com.xertica.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EntityScan(basePackages = "com.xertica.faq.model")
@EnableJpaRepositories(
  basePackages = "com.xertica.faq.repository",
  entityManagerFactoryRef = "faqEntityManagerFactory",
  transactionManagerRef = "faqTransactionManager"
)
public class FaqDbConfig {

  @Bean
  @ConfigurationProperties("app.datasource.faq")
  public DataSourceProperties faqDsProps() {
    return new DataSourceProperties();
  }

  @Bean(name = "faqDataSource")
  public DataSource faqDataSource(@Qualifier("faqDsProps") DataSourceProperties props) {
    return props.initializeDataSourceBuilder().build();
  }

  @Bean(name = "faqEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean faqEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("faqDataSource") DataSource ds) {

    Map<String, Object> jpaProps = new HashMap<>();
    jpaProps.put("hibernate.hbm2ddl.auto", "none");
    jpaProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

    return builder
        .dataSource(ds)
        .packages("com.xertica.faq.model")
        .persistenceUnit("faq")
        .properties(jpaProps)
        .build();
  }

  @Bean(name = "faqTransactionManager")
  public PlatformTransactionManager faqTransactionManager(
      @Qualifier("faqEntityManagerFactory") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}