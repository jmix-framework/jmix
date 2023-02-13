package io.jmix.autoconfigure.appsettingsflowui;

import io.jmix.appsettingsflowui.AppSettingsFlowUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AppSettingsFlowUiConfiguration.class})
public class AppSettingsFlowUiAutoConfiguration {
}
