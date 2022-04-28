package io.jmix.flowui.impl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import io.jmix.flowui.Screens;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenInfo;
import io.jmix.flowui.screen.ScreenRegistry;
import org.springframework.stereotype.Component;

@Component("flowui_Screens")
public class ScreensImpl implements Screens {

    protected ScreenRegistry screenRegistry;

    public ScreensImpl(ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
    }

    @Override
    public Screen create(String screenId) {
        ScreenInfo screenInfo = screenRegistry.getScreenInfo(screenId);
        return create(screenInfo.getControllerClass());
    }

    @Override
    public <T extends Screen> T create(Class<T> screenClass) {
        return Instantiator.get(UI.getCurrent()).getOrCreate(screenClass);
    }
}
