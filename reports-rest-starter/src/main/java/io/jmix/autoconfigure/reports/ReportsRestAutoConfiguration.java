package io.jmix.autoconfigure.reports;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reportsrest.ReportsRestConfiguration;
import io.jmix.reportsrest.security.event.ReportBeforeInvocationEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, ReportsConfiguration.class, ReportsRestConfiguration.class})
public class ReportsRestAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = "io.jmix.securityoauth2.SecurityOAuth2Configuration")
    protected ReportBeforeInvocationEventListener reportBeforeInvocationEventListener() {
        return new ReportBeforeInvocationEventListener();
    }
}
