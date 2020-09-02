package io.jmix.autoconfigure.reports;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class})
public class ReportsAutoConfiguration {
}
