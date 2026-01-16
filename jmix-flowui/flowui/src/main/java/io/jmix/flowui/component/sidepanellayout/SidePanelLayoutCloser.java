/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.sidepanellayout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.sidepanellayout.JmixSidePanelLayoutCloser;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

/**
 * The button for closing the side panel in a {@link SidePanelLayout}.
 */
public class SidePanelLayoutCloser extends JmixSidePanelLayoutCloser implements ApplicationContextAware,
        InitializingBean {

    protected ApplicationContext applicationContext;

    protected SidePanelLayout sidePanelLayout;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    protected void initComponent() {
        setIconInternal(applicationContext.getBean(Icons.class)
                .get(JmixFontIcon.SIDE_PANEL_LAYOUT_CLOSER));
    }

    @Nullable
    public SidePanelLayout getSidePanelLayout() {
        return sidePanelLayout;
    }

    public void setSidePanelLayout(@Nullable SidePanelLayout sidePanelLayout) {
        this.sidePanelLayout = sidePanelLayout;
    }

    @Override
    protected void onClick(ClickEvent<Button> event) {
        super.onClick(event);

        if (sidePanelLayout == null) {
            throw new IllegalStateException(SidePanelLayout.class.getSimpleName() + " is not set");
        }

        sidePanelLayout.closeSidePanel();
    }
}
