/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.sys.autowire;

import io.jmix.flowui.view.View;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class describes autowire context that is used to autowire dependencies in view.
 */
public class ViewAutowireContext implements DependencyInjector.AutowireContext<View<?>> {

    protected final View<?> view;

    protected final Collection<Object> autowired = new HashSet<>();

    public ViewAutowireContext(View<?> view) {
        this.view = view;
    }

    @Override
    public View<?> getTarget() {
        return view;
    }

    @Override
    public Collection<Object> getAutowired() {
        return autowired;
    }
}
