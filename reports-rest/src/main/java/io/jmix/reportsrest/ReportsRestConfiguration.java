package io.jmix.reportsrest;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.reports.ReportsConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Order(200)
@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@EnableWebSecurity
@JmixModule(dependsOn = {CoreConfiguration.class, ReportsConfiguration.class})
@PropertySource(name = "io.jmix.reportsrest", value = "classpath:/io/jmix/reportsrest/module.properties")
public class ReportsRestConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/rest/reports/**")
                .authorizeRequests().anyRequest().authenticated()
                .and().csrf();
    }

}
