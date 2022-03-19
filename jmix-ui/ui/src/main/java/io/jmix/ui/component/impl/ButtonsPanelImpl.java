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

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.VisibilityChangeNotifier;
import io.jmix.ui.widget.JmixFlowLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ButtonsPanelImpl extends FlowBoxLayoutImpl implements ButtonsPanel, VisibilityChangeNotifier {

    public static final String BUTTONS_PANEL_STYLENAME = "jmix-buttons-panel";

    protected boolean alwaysVisible = false;

    public ButtonsPanelImpl() {
        initComponent(component);
    }

    protected void initComponent(JmixFlowLayout component) {
        setSpacing(true);
        setMargin(false);

        component.addStyleName(BUTTONS_PANEL_STYLENAME);
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        component.addStyleName(BUTTONS_PANEL_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(BUTTONS_PANEL_STYLENAME, ""));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        publish(VisibilityChangeEvent.class,
                new VisibilityChangeEvent(this, visible));
    }

    @Override
    public Subscription addVisibilityChangeListener(Consumer<VisibilityChangeEvent> listener) {
        return getEventHub().subscribe(VisibilityChangeEvent.class, listener);
    }

    @Override
    public void setAlwaysVisible(boolean alwaysVisible) {
        this.alwaysVisible = alwaysVisible;
    }

    @Override
    public boolean isAlwaysVisible() {
        return alwaysVisible;
    }
}