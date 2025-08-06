package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.Component;
import jakarta.annotation.Nullable;

public interface TextUserMenuItem extends UserMenuItem, UserMenuItem.HasClickListener<TextUserMenuItem> {

    String getText();

    void setText(String text);

    @Nullable
    Component getIcon();

    void setIcon(@Nullable Component icon);
}
