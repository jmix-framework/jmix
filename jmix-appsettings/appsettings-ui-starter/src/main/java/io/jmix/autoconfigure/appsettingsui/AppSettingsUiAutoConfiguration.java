package io.jmix.autoconfigure.appsettingsui;

import io.jmix.appsettingsui.AppSettingsUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AppSettingsUiConfiguration.class})
public class AppSettingsUiAutoConfiguration {
}
