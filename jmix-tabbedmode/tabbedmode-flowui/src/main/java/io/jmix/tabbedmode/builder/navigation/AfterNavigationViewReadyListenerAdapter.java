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

package io.jmix.tabbedmode.builder.navigation;

import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.SupportsAfterViewNavigationHandler.AfterViewNavigationEvent;
import io.jmix.tabbedmode.builder.ViewReadyEvent;

import java.util.function.Consumer;

public class AfterNavigationViewReadyListenerAdapter<V extends View<?>> implements Consumer<ViewReadyEvent<V>> {

    protected final Consumer<AfterViewNavigationEvent<V>> delegate;

    public AfterNavigationViewReadyListenerAdapter(Consumer<AfterViewNavigationEvent<V>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void accept(ViewReadyEvent<V> event) {
        delegate.accept(new AfterViewNavigationEvent<>(this, event.getSource()));
    }
}
