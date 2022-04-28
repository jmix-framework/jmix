package io.jmix.flowui;

import io.jmix.flowui.screen.Screen;

public interface Screens {

    Screen create(String screenId);

    <T extends Screen> T create(Class<T> screenClass);
}
