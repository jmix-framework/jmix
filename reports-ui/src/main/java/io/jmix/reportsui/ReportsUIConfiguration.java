package io.jmix.reportsui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reportsui.screen.definition.edit.crosstab.CrossTabTableDecorator;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.uiexport.UiExportConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@Import(UiExportConfiguration.class)
@JmixModule(dependsOn = {CoreConfiguration.class, UiConfiguration.class, ReportsConfiguration.class})
@PropertySource(name = "io.jmix.reportsui", value = "classpath:/io/jmix/reportsui/module.properties")
public class ReportsUIConfiguration {

    @Bean("reports_ReportsUiControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.reportsui.screen"));
        return uiControllers;
    }

    @Bean("reports_ReportsUiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.reportsui.action"));
        return actionsConfiguration;
    }

    @Bean("report_DataSetFactory")
    public DataSetFactory dataSetFactory() {
        return new DataSetFactory();
    }

    @Bean("reports_CrossTabOrientationTableDecorator")
    public CrossTabTableDecorator crossTabTableDecorator() {
        return new CrossTabTableDecorator();
    }

}
