package io.jmix.flowui.screen;

import io.jmix.flowui.component.layout.ScreenLayout;

public class StandardScreen extends Screen<ScreenLayout> {

    @Override
    protected ScreenLayout initContent() {
        ScreenLayout content = super.initContent();
        content.setSizeFull();

        return content;
    }
}
