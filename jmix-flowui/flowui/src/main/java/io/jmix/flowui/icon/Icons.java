package io.jmix.flowui.icon;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.icon.IconFactory;

public interface Icons {

    Component get(IconFactory icon);

    Component get(String iconName);
}
