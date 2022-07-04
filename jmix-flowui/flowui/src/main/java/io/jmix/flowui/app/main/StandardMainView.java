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
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.UiControllerUtils;
import io.jmix.flowui.sys.ViewSupport;

import java.util.Optional;

public class StandardMainView extends View<AppLayout> implements RouterLayout {

    @Override
    public void showRouterLayoutContent(HasElement content) {
        getContent().showRouterLayoutContent(content);

        updateTitle();
    }

    private void updateTitle() {
        getTitleComponent()
                .filter(c -> c instanceof HasText)
                .ifPresent(c -> ((HasText) c).setText(getTitleFromOpenedView()));
    }

    protected Optional<Component> getTitleComponent() {
        return UiComponentUtils.findComponent(getContent(), "viewTitle");
    }

    private String getTitleFromOpenedView() {
        if (getContent().getContent() instanceof View) {
            return getApplicationContext().getBean(ViewSupport.class)
                    .getLocalizedPageTitle((View<?>) getContent().getContent());
        } else {
            return UiControllerUtils.getPageTitle(getContent());
        }
    }
}
