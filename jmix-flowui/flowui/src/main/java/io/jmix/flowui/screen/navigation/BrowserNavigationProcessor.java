package io.jmix.flowui.screen.navigation;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.sys.ScreenSupport;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_BrowserNavigationProcessor")
public class BrowserNavigationProcessor extends AbstractNavigationProcessor<BrowserNavigator<?>> {

    public BrowserNavigationProcessor(ScreenSupport screenSupport,
                                      ScreenRegistry screenRegistry,
                                      ScreenNavigationSupport navigationSupport) {
        super(screenSupport, screenRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends Screen> inferScreenClass(BrowserNavigator<?> navigator) {
        return screenRegistry.getLookupScreen(navigator.getEntityClass()).getControllerClass();
    }
}
