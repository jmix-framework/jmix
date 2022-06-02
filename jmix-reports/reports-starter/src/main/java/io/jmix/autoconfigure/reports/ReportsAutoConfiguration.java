package io.jmix.autoconfigure.reports;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.reports.ReportsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, ReportsConfiguration.class})
public class ReportsAutoConfiguration {
}
