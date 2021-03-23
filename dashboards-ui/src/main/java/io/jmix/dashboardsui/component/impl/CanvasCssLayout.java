/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.component.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.dashboards.model.visualmodel.CssLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.LayoutClickNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class CanvasCssLayout extends AbstractCanvasLayout {

    public static final String NAME = "canvasCssLayout";

    private static Logger log = LoggerFactory.getLogger(DashboardImpl.class);

    protected io.jmix.ui.component.CssLayout cssLayout;

    public CanvasCssLayout init(CssLayout cssLayoutModel) {
        init(cssLayoutModel, io.jmix.ui.component.CssLayout.class);
        cssLayout = (io.jmix.ui.component.CssLayout) delegate;

        cssLayout.setStyleName(cssLayoutModel.getStyleName());
        cssLayout.setResponsive(cssLayoutModel.getResponsive());
        return this;
    }

    @Override
    public io.jmix.ui.component.CssLayout getDelegate() {
        return cssLayout;
    }

    public void addComponent(Component component) {
        cssLayout.add(component);
    }

    @Override
    public Subscription addLayoutClickListener(Consumer<LayoutClickNotifier.LayoutClickEvent> listener) {
        log.info("Click listener is not supported yet in Css layout");
        return () -> {
        };
    }
}
