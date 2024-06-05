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

package io.jmix.flowui;

import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.xml.layout.ComponentLoader;


/**
 * Factory for creating {@link Fragment}s.
 */
@Experimental
public interface Fragments {

    /**
     * Creates a fragment instance by its controller class.
     * <p>
     * For example:
     * <pre>{@code
     *    AddressFragment addressFragment = fragments.create(this, AddressFragment.class);
     *    getContent().add(addressFragment);
     * }</pre>
     *
     * @param parent        parent UI controller
     * @param fragmentClass fragment controller class
     * @param <F>           fragment type
     * @return fully initialized fragment instance
     */
    <F extends Fragment<?>> F create(FragmentOwner parent, Class<F> fragmentClass);

    /**
     * Initializes passed fragment by processing {@link FragmentDescriptor}.
     *
     * @param hostContext parent controller loader context. Used to get additional
     *                    data that is needed for correct initialization. For example,
     *                    provided data components
     * @param fragment    fragment to initialize
     */
    void init(ComponentLoader.Context hostContext, Fragment<?> fragment);
}
