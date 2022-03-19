package io.jmix.autoconfigure.reports;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.reports.ReportsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, ReportsConfiguration.class})
public class ReportsAutoConfiguration {
}
