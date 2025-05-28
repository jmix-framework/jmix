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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.Location;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A wrapper component for {@link JmixBreadcrumbs} that manages a stack of views.
 */
public class ViewBreadcrumbs extends Composite<JmixBreadcrumbs> implements ApplicationContextAware {

    protected UiComponents uiComponents;

    protected Deque<BreadcrumbMappings> mappings = new ArrayDeque<>(4);

    protected Consumer<BreadcrumbsNavigationContext> navigationHandler;
    protected boolean visibleExplicitly = true;

    public ViewBreadcrumbs() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        uiComponents = applicationContext.getBean(UiComponents.class);
    }

    @Override
    protected JmixBreadcrumbs initContent() {
        JmixBreadcrumbs content = super.initContent();
        content.setClassName("jmix-view-breadcrumbs");

        return content;
    }

    /**
     * Sets a handler to be invoked when an individual breadcrumb is clicked.
     *
     * @param handler a handler to set
     */
    public void setNavigationHandler(@Nullable Consumer<BreadcrumbsNavigationContext> handler) {
        this.navigationHandler = handler;
    }

    /**
     * Returns all views represented by this breadcrumbs component.
     *
     * @return a collection containing all {@link View} instances
     */
    public Deque<View<?>> getViews() {
        return mappings.stream()
                .map(BreadcrumbMappings::getView)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    /**
     * returns the information about the current view and its associated location.
     * If no views are present in the stack, returns {@code null}.
     *
     * @return an instance of {@link ViewInfo} containing the current view and its location,
     * or {@code null} if no views are available.
     */
    @Nullable
    public ViewInfo getCurrentViewInfo() {
        if (mappings.isEmpty()) {
            return null;
        } else {
            BreadcrumbMappings mappings = this.mappings.getLast();
            return new ViewInfo(mappings.getView(), mappings.getLocation());
        }
    }

    /**
     * Adds the given {@link View} to the view stack, registers {@link Location}
     * mapping and updates visibility state.
     *
     * @param view     a view to add
     * @param location associated location
     */
    public void addView(View<?> view, Location location) {
        JmixBreadcrumb breadcrumb = createBreadcrumb(view);

        BreadcrumbMappings mappings = new BreadcrumbMappings(breadcrumb, view, location);
        if (this.mappings.add(mappings)) {
            getContent().add(breadcrumb);
        }

        updateVisibility();
    }

    protected JmixBreadcrumb createBreadcrumb(View<?> view) {
        JmixBreadcrumb breadcrumb = uiComponents.create(JmixBreadcrumb.class);
        breadcrumb.setText(ViewControllerUtils.getPageTitle(view));
        breadcrumb.setClickHandler(this::navigationClicked);
        breadcrumb.setTabIndex(-1);

        return breadcrumb;
    }

    /**
     * Removes the last {@link View} from the view stack and updates visibility state.
     */
    public void removeView() {
        if (!mappings.isEmpty()) {
            BreadcrumbMappings removed = mappings.removeLast();
            getContent().remove(removed.getBreadcrumb());
        }

        updateVisibility();
    }

    /**
     * Updates {@link Location} mapping of the given {@link View}.
     *
     * @param view     view to update the location mapping for
     * @param location a new location
     */
    public void updateViewLocation(View<?> view, Location location) {
        BreadcrumbMappings breadcrumbMappings = findMappings(view)
                .orElseThrow(() ->
                        new IllegalArgumentException("There is no breadcrumb mappings for the given view '%s'"
                                .formatted(view.getId().orElse(view.getClass().getSimpleName()))));

        breadcrumbMappings.setLocation(location);
    }

    protected Optional<BreadcrumbMappings> findMappings(View<?> view) {
        for (BreadcrumbMappings mappings : mappings) {
            if (Objects.equals(mappings.getView(), view)) {
                return Optional.of(mappings);
            }
        }

        return Optional.empty();
    }

    protected void navigationClicked(JmixBreadcrumb.ClickEvent<JmixBreadcrumb> event) {
        if (navigationHandler != null) {
            findView(event.getSource()).ifPresent(view ->
                    navigationHandler.accept(new BreadcrumbsNavigationContext(this, view)));
        }
    }

    protected Optional<View<?>> findView(JmixBreadcrumb breadcrumb) {
        for (BreadcrumbMappings mappings : mappings) {
            if (Objects.equals(mappings.getBreadcrumb(), breadcrumb)) {
                return Optional.of(mappings.getView());
            }
        }

        return Optional.empty();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visibleExplicitly = visible;

        setVisibleInternal(visible);
    }

    protected void setVisibleInternal(boolean visible) {
        super.setVisible(visible && visibleExplicitly);
    }

    protected void updateVisibility() {
        setVisibleInternal(mappings.size() > 1);
    }

    /**
     * @param breadcrumbs
     * @param view
     */
    public record BreadcrumbsNavigationContext(ViewBreadcrumbs breadcrumbs, View<?> view) {
    }

    /**
     * Represents the information about a {@link View} and its associated {@link Location}.
     *
     * @param view     the {@link View} instance
     * @param location the {@link Location} associated with the {@link View} instance
     */
    public record ViewInfo(View<?> view, Location location) {
    }

    protected static class BreadcrumbMappings {

        protected final JmixBreadcrumb breadcrumb;
        protected final View<?> view;
        protected Location location;

        protected BreadcrumbMappings(JmixBreadcrumb breadcrumb, View<?> view, Location location) {
            this.breadcrumb = breadcrumb;
            this.view = view;
            this.location = location;
        }

        public JmixBreadcrumb getBreadcrumb() {
            return breadcrumb;
        }

        public View<?> getView() {
            return view;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (BreadcrumbMappings) obj;
            return Objects.equals(this.breadcrumb, that.breadcrumb) &&
                    Objects.equals(this.view, that.view) &&
                    Objects.equals(this.location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(breadcrumb, view, location);
        }

        @Override
        public String toString() {
            return "BreadcrumbMappings[" +
                    "breadcrumb=" + breadcrumb + ", " +
                    "view=" + view + ", " +
                    "location=" + location + ']';
        }

    }
}
