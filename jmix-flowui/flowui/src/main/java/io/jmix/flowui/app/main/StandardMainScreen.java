/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.app.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.RouterLayout;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.UiControllerUtils;
import io.jmix.flowui.sys.ScreenSupport;

import java.util.Optional;

public class StandardMainScreen extends Screen<AppLayout> implements RouterLayout {

    @Override
    public void showRouterLayoutContent(HasElement content) {
        getContent().showRouterLayoutContent(content);

        updateScreenTitle();
    }

    protected void updateScreenTitle() {
        getScreenTitleComponent()
                .filter(c -> c instanceof HasText)
                .ifPresent(c -> ((HasText) c).setText(getTitleFromOpenedScreen()));
    }

    protected Optional<Component> getScreenTitleComponent() {
        return UiComponentUtils.findComponent(getContent(), "screenTitle");
    }

    private String getTitleFromOpenedScreen() {
        if (getContent().getContent() instanceof Screen) {
            return getApplicationContext().getBean(ScreenSupport.class)
                    .getLocalizedPageTitle((Screen<?>) getContent().getContent());
        } else {
            return UiControllerUtils.getPageTitle(getContent());
        }
    }
}
