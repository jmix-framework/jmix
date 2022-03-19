package io.jmix.autoconfigure.appsettingsui;

import io.jmix.appsettingsui.AppSettingsUiConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AppSettingsUiConfiguration.class})
public class AppSettingsUiAutoConfiguration {
}
