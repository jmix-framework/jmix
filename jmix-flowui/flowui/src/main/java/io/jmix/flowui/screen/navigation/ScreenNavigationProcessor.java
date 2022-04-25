package io.jmix.flowui.screen.navigation;

import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.sys.ScreenSupport;
import org.springframework.stereotype.Component;

@Component("flowui_ScreenNavigationProcessor")
public class ScreenNavigationProcessor extends AbstractNavigationProcessor<ScreenNavigator> {

    public ScreenNavigationProcessor(ScreenSupport screenSupport,
                                     ScreenRegistry screenRegistry,
                                     ScreenNavigationSupport navigationSupport) {
        super(screenSupport, screenRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends Screen> inferScreenClass(ScreenNavigator navigator) {
        throw new IllegalStateException("Can't open a screen. " +
                "Either screen id or screen class must be defined");
    }
}
