/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.component.breadcrumbs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;

@Tag("jmix-breadcrumbs")
@JsModule("./src/breadcrumbs/jmix-breadcrumbs.js")
public class JmixBreadcrumbs extends Component {

    protected JmixBreadcrumb homeLink;

    public JmixBreadcrumbs() {
    }

    public boolean isHomeLinkVisible() {
        return homeLink != null;
    }

    public void setHomeLinkVisible(boolean visible) {
        if (visible && homeLink == null) {
            homeLink = createHomeLink();
            getElement().insertChild(0, homeLink.getElement());
        } else if (homeLink != null) {
            homeLink.removeFromParent();
        }
    }

    private JmixBreadcrumb createHomeLink() {
        JmixBreadcrumb breadcrumb = new JmixBreadcrumb()
                .withText("Home") // TODO: gg, message
                .withClickHandler(event -> {
                    Notification.show("Home clicked");
                });
        // TODO: gg, icon
        breadcrumb.addClassName("jmix-breadcrumb-home-link");

        return breadcrumb;
    }

    public void add(JmixBreadcrumb breadcrumb) {
        // TODO: gg, check for null
        getElement().appendChild(breadcrumb.getElement());
    }

    public void remove(JmixBreadcrumb breadcrumb) {
        // TODO: gg, check for null
        getElement().removeChild(breadcrumb.getElement());
    }
}
