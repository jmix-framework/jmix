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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.Location;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ViewBreadcrumbs extends Composite<JmixBreadcrumbs> implements ApplicationContextAware {

    // TODO: gg, rename
    protected static final String BREADCRUMBS_VISIBLE_WRAP_STYLE = "jmix-breadcrumbs-visible";

    protected Deque<View<?>> views = new ArrayDeque<>(4);
    protected BiMap<View<?>, JmixBreadcrumb> viewBreadcrumb = HashBiMap.create(4);
    protected Map<View<?>, Location> viewLocation = new HashMap<>(4);

    protected Consumer<BreadcrumbsNavigationContext> navigationHandler;
    protected boolean visibleExplicitly = true;

    public ViewBreadcrumbs() {
    }

    @Override
    protected JmixBreadcrumbs initContent() {
        JmixBreadcrumbs content = super.initContent();
        content.setClassName("jmix-view-breadcrumbs");

        return content;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // TODO: gg, implement
    }

    public void setNavigationHandler(@Nullable Consumer<BreadcrumbsNavigationContext> handler) {
        this.navigationHandler = handler;
    }

    public Deque<View<?>> getViews() {
        // TODO: gg, UnmodifiableDeque?
        return new ArrayDeque<>(views);
    }

    @Nullable
    public ViewInfo getCurrentViewInfo() {
        if (views.isEmpty()) {
            return null;
        } else {
            View<?> view = views.getLast();
            return new ViewInfo(view, viewLocation.get(view));
        }
    }

    public void addView(View<?> view, Location resolvedLocation) {
        if (views.add(view)) {
            // TODO: gg, add class that stores view and location instead of maps
            JmixBreadcrumb breadcrumb = new JmixBreadcrumb();
            // TODO: gg, replace StringUtils
            breadcrumb.setText(ViewControllerUtils.getPageTitle(view));
            breadcrumb.setClickHandler(this::navigationClicked);
            breadcrumb.setTabIndex(-1);

            getContent().add(breadcrumb);
            viewBreadcrumb.put(view, breadcrumb);
            viewLocation.put(view, resolvedLocation);
        }


        if (views.size() > 1 /*&& workAreaMode == Mode.TABBED*/) {
            setVisibleInternal(true);
        }

        adjustParentStyles();
    }

    public void removeView() {
        if (!views.isEmpty()) {
            View<?> removed = views.removeLast();
            JmixBreadcrumb breadcrumb = viewBreadcrumb.remove(removed);
            getContent().remove(breadcrumb);
        }

        if (views.size() <= 1/* && workAreaMode == Mode.TABBED*/) {
            setVisibleInternal(false);
        }
    }

    protected void navigationClicked(ClickEvent<JmixBreadcrumb> event) {
        View<?> view = viewBreadcrumb.inverse().get(event.getSource());
        if (navigationHandler != null && view != null) {
            navigationHandler.accept(new BreadcrumbsNavigationContext(this, view));
        }
    }

    protected void adjustParentStyles() {
        getParent().ifPresent(parent -> {
            if (isVisible()) {
                parent.addClassName(BREADCRUMBS_VISIBLE_WRAP_STYLE);
            } else {
                parent.removeClassName(BREADCRUMBS_VISIBLE_WRAP_STYLE);
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        this.visibleExplicitly = visible;

        setVisibleInternal(visible);
    }

    protected void setVisibleInternal(boolean visible) {
        super.setVisible(visible && visibleExplicitly);

        adjustParentStyles();
    }

    public record BreadcrumbsNavigationContext(ViewBreadcrumbs breadcrumbs, View<?> view) {
    }

    public record ViewInfo(View<?> view, @Nullable Location location) {
    }
}
