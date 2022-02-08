package io.jmix.appsettingsui;

import io.jmix.appsettings.AppSettingsConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@JmixModule(dependsOn = {
        UiConfiguration.class,
        AppSettingsConfiguration.class
})
@PropertySource(name = "io.jmix.appsettingsui", value = "classpath:/io/jmix/appsettingsui/module.properties")
public class AppSettingsUiConfiguration {

    @Bean("appset_UiControllersConfiguration")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.appsettingsui.screen"));
        return uiControllers;
    }
}
