package io.jmix.flowui.icon.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.icon.IconFactory;
import io.jmix.flowui.kit.icon.JmixFontIcon;

@org.springframework.stereotype.Component("flowui_Icons")
public class IconsImpl implements Icons {

    @Override
    public Component get(IconFactory icon) {
        Preconditions.checkNotNullArgument(icon);
        return get(icon.name());
    }

    @Override
    public Component get(String iconName) {
        Preconditions.checkNotNullArgument(iconName);

        if (iconName.contains(":")) {
            String[] parts = iconName.split(":");
            if (parts.length != 2) {
                throw new IllegalStateException("Unexpected number of icon parts, must be two");
            }

            return new Icon(parts[0], parts[1]);
        } else {
            JmixFontIcon jmixFontIcon = JmixFontIcon.fromName(iconName);
            return jmixFontIcon != null
                    ? jmixFontIcon.create()
                    : VaadinIcon.valueOf(iconName).create();
        }
    }
}
