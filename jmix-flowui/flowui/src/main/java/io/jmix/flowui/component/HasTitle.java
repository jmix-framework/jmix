package io.jmix.flowui.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.HasElement;

import javax.annotation.Nullable;

public interface HasTitle extends HasElement {

    @Nullable
    default String getTitle() {
        return getElement().getProperty("title");
    }

    default void setTitle(@Nullable String title) {
        String titleValue = Strings.nullToEmpty(title);

        getElement().setProperty("title", titleValue);
        // TODO: gg, leave here?
        getElement().setAttribute("aria-label", titleValue);
    }
}
