package io.jmix.flowui;

import io.jmix.flowui.view.View;

public interface Views {

    View create(String viewId);

    <T extends View> T create(Class<T> viewClass);
}
