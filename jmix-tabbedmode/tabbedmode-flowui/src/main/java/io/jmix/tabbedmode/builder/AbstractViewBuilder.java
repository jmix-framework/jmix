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

package io.jmix.tabbedmode.builder;

import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.view.ViewOpenMode;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

public class AbstractViewBuilder<V extends View<?>, B extends AbstractViewBuilder<V, B>> {

    protected final View<?> origin;
    protected final Function<B, V> buildHandler;
    protected final Consumer<ViewOpeningContext> openHandler;

    protected String viewId;
    protected Class<V> viewClass;

    protected ViewOpenMode openMode = ViewOpenMode.THIS_TAB;

    protected Consumer<ViewReadyEvent<V>> readyListener;
    protected Consumer<ViewAfterCloseEvent<V>> afterCloseListener;
    protected Consumer<V> viewConfigurer;

    protected V builtView;

    public AbstractViewBuilder(View<?> origin,
                               Function<B, V> buildHandler,
                               Consumer<ViewOpeningContext> openHandler) {
        this.origin = origin;
        this.buildHandler = buildHandler;
        this.openHandler = openHandler;
    }

    public View<?> getOrigin() {
        return origin;
    }

    public Optional<String> getViewId() {
        return Optional.ofNullable(viewId);
    }

    public Optional<Class<V>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }

    public ViewOpenMode getOpenMode() {
        return openMode;
    }

    public Optional<Consumer<ViewReadyEvent<V>>> getReadyListener() {
        return Optional.ofNullable(readyListener);
    }

    public Optional<Consumer<ViewAfterCloseEvent<V>>> getAfterCloseListener() {
        return Optional.ofNullable(afterCloseListener);
    }

    public Optional<Consumer<V>> getViewConfigurer() {
        return Optional.ofNullable(viewConfigurer);
    }

    @SuppressWarnings("unchecked")
    public B withOpenMode(ViewOpenMode openMode) {
        Preconditions.checkNotNullArgument(openMode);

        this.openMode = openMode;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withReadyListener(@Nullable Consumer<ViewReadyEvent<V>> readyListener) {
        this.readyListener = readyListener;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withAfterCloseListener(@Nullable Consumer<ViewAfterCloseEvent<V>> afterCloseListener) {
        this.afterCloseListener = afterCloseListener;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        this.viewConfigurer = viewConfigurer;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public V build() {
        checkState(builtView == null, "%s already built"
                .formatted(View.class.getSimpleName()));

        builtView = buildHandler.apply((B) this);
        return builtView;
    }

    public V open() {
        if (builtView == null) {
            builtView = build();
        }

        openHandler.accept(createViewOpeningContext());

        return builtView;
    }

    protected ViewOpeningContext createViewOpeningContext() {
        return ViewOpeningContext.create(builtView, openMode);
    }
}
