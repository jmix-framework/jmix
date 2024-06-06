package io.jmix.searchelasticsearch;

import io.jmix.core.annotation.JmixModule;
import io.jmix.search.SearchConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {SearchConfiguration.class})
public class SearchElasticsearchConfiguration {
}
