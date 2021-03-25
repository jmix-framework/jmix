/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import io.jmix.ui.MainTabSheetMode;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiProperties;
import org.springframework.context.ApplicationContext;
import io.jmix.ui.component.TabWindow;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.widget.JmixSingleModeContainer;
import io.jmix.ui.widget.TabWindowContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class TabWindowImpl extends WindowImpl implements TabWindow {

    private static final Logger log = LoggerFactory.getLogger(TabWindowImpl.class);

    protected ContentSwitchMode contentSwitchMode = ContentSwitchMode.DEFAULT;

    protected ApplicationContext applicationContext;

    public TabWindowImpl() {
        setSizeFull();

        // default caption for Tab is not empty
        setCaption(" ");
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setIcon(@Nullable String icon) {
        super.setIcon(icon);

        if (component.isAttached()) {
            TabSheet.Tab tabWindow = findTab();
            if (tabWindow != null) {
                IconResolver iconResolver = applicationContext.getBean(IconResolver.class);
                tabWindow.setIcon(iconResolver.getIconResource(icon));
            }
        }
    }

    @Override
    public void setCloseable(boolean closeable) {
        super.setCloseable(closeable);

        if (component.isAttached()) {
            TabSheet.Tab tabWindow = findTab();
            if (tabWindow != null) {
                tabWindow.setClosable(closeable);
            }
        }
    }

    @Override
    public void setCaption(@Nullable String caption) {
        super.setCaption(caption);

        if (component.isAttached()) {
            updateCaptionAndDescription();
        }
    }

    @Override
    public void setDescription(@Nullable String description) {
        super.setDescription(description);

        if (component.isAttached()) {
            updateCaptionAndDescription();
        }
    }

    protected void updateCaptionAndDescription() {
        TabSheet.Tab tabWindow = findTab();
        if (tabWindow != null) {
            String tabCaption = formatTabCaption();
            String tabDescription = formatTabDescription();

            tabWindow.setCaption(tabCaption);

            if (!Objects.equals(tabCaption, tabDescription)) {
                tabWindow.setDescription(tabDescription);
            } else {
                tabWindow.setDescription(null);
            }

            ((TabWindowContainer) tabWindow.getComponent()).getBreadCrumbs().update();
        } else {
            TabWindowContainer layout = (TabWindowContainer) asSingleWindow();
            if (layout != null) {
                layout.getBreadCrumbs().update();
            }
        }
    }

    @Nullable
    protected TabSheet.Tab asTabWindow() {
        if (component.isAttached()) {
            com.vaadin.ui.Component parent = component;
            while (parent != null) {
                if (parent.getParent() instanceof TabSheet) {
                    return ((TabSheet) parent.getParent()).getTab(parent);
                }

                parent = parent.getParent();
            }
        }
        return null;
    }

    @Nullable
    protected Layout asSingleWindow() {
        if (component.isAttached()) {
            com.vaadin.ui.Component parent = component;
            while (parent != null) {
                if (parent.getParent() instanceof JmixSingleModeContainer) {
                    return (Layout) parent;
                }

                parent = parent.getParent();
            }
        }
        return null;
    }

    @Nullable
    protected TabSheet.Tab findTab() {
        if (component.isAttached()) {
            com.vaadin.ui.Component parent = component;
            while (parent != null) {
                if (parent.getParent() instanceof TabSheet) {
                    return ((TabSheet) parent.getParent()).getTab(parent);
                }

                parent = parent.getParent();
            }
        }
        return null;
    }

    @Override
    public String formatTabCaption() {
        String s = formatTabDescription();

        int maxLength = applicationContext.getBean(UiProperties.class).getMainTabCaptionLength();
        if (s.length() > maxLength) {
            return s.substring(0, maxLength) + "...";
        } else {
            return s;
        }
    }

    @Override
    public String formatTabDescription() {
        if (!StringUtils.isEmpty(getDescription())) {
            return String.format("%s: %s", getCaption(), getDescription());
        } else {
            return Strings.nullToEmpty(getCaption());
        }
    }

    @Override
    public ContentSwitchMode getContentSwitchMode() {
        return contentSwitchMode;
    }

    @Override
    public void setContentSwitchMode(ContentSwitchMode mode) {
        checkNotNullArgument(mode, "Content switch mode can't be null. Use ContentSwitchMode.DEFAULT option instead");

        MainTabSheetMode tabSheetMode = applicationContext.getBean(UiComponentProperties.class)
                .getMainTabSheetMode();
        if (tabSheetMode != MainTabSheetMode.MANAGED) {
            log.debug("Content switch mode can be set only for the managed main TabSheet. Current invocation will be ignored.");
        }

        this.contentSwitchMode = mode;
    }
}