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

import io.jmix.flowui.fragment.Fragment;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class describes autowire context that is used to autowire dependencies in {@link Fragment}.
 */
public class FragmentAutowireContext implements DependencyInjector.AutowireContext<Fragment<?>> {

    protected final Fragment<?> fragment;

    protected final Collection<Object> autowired = new HashSet<>();

    public FragmentAutowireContext(Fragment<?> fragment) {
        this.fragment = fragment;
    }

    @Override
    public Fragment<?> getTarget() {
        return fragment;
    }

    @Override
    public Collection<Object> getAutowired() {
        return autowired;
    }
}
