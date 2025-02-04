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

import com.vaadin.flow.component.ComponentEventListener;
import io.jmix.flowui.view.View;

import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class ViewAfterCloseListenerAdapter<V extends View<?>> implements ComponentEventListener<View.AfterCloseEvent> {

    protected final Consumer<ViewAfterCloseEvent<V>> delegate;

    public ViewAfterCloseListenerAdapter(Consumer<ViewAfterCloseEvent<V>> delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComponentEvent(View.AfterCloseEvent event) {
        delegate.accept(new ViewAfterCloseEvent(event.getSource(), event.getCloseAction()));
    }
}
