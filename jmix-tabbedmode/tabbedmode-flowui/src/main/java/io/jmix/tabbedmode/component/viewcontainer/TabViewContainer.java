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

package io.jmix.tabbedmode.component.viewcontainer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import org.springframework.lang.Nullable;


@Tag("jmix-view-container")
@JsModule("./src/viewcontainer/jmix-view-container.js")
public class TabViewContainer extends Component implements ViewContainer, HasSize, HasStyle {

    protected ViewBreadcrumbs breadcrumbs;
    protected View<?> view;

    @Nullable
    @Override
    public ViewBreadcrumbs getBreadcrumbs() {
        return breadcrumbs;
    }

    @Override
    public void setBreadcrumbs(@Nullable ViewBreadcrumbs breadcrumbs) {
        if (this.breadcrumbs != null) {
            remove(this.breadcrumbs);
        }

        this.breadcrumbs = breadcrumbs;

        if (breadcrumbs != null) {
            setBreadcrumbsInternal(breadcrumbs);
        }
    }

    protected void setBreadcrumbsInternal(ViewBreadcrumbs breadcrumbs) {
        add(breadcrumbs);
        breadcrumbs.getElement().setAttribute("slot", "breadcrumbs");
    }

    @Override
    public void removeBreadcrumbs() {
        setBreadcrumbs(null);
    }

    @Nullable
    @Override
    public View<?> getView() {
        return view;
    }

    @Override
    public void setView(@Nullable View<?> view) {
        if (this.view != null) {
            remove(this.view);
        }

        this.view = view;

        if (view != null) {
            removeAll();

            if (this.breadcrumbs != null) {
                setBreadcrumbsInternal(this.breadcrumbs);
            }

            add(view);
        }
    }

    @Override
    public void removeView() {
        setView(null);
    }

    private void add(Component component) {
        getElement().appendChild(component.getElement());
    }

    protected void remove(Component component) {
        if (getElement().equals(component.getElement().getParent())) {
            component.getElement().removeAttribute("slot");
            getElement().removeChild(component.getElement());
        } else {
            throw new IllegalArgumentException("The given component ("
                    + component + ") is not a child of this component");
        }
    }

    protected void removeAll() {
        getElement().getChildren()
                .forEach(child -> child.removeAttribute("slot"));
        getElement().removeAllChildren();
    }
}
