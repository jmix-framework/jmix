package io.jmix.flowui.view;

import io.jmix.flowui.component.layout.ViewLayout;

public class StandardView extends View<ViewLayout> {

    @Override
    protected ViewLayout initContent() {
        ViewLayout content = super.initContent();
        content.setSizeFull();

        return content;
    }
}
