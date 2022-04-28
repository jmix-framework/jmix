/*
 * Copyright 2019 Haulmont.
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

package io.jmix.flowui.xml.layout;

import org.dom4j.Element;

import javax.annotation.Nullable;

/**
 * Marker interface for component loaders.
 */
@SuppressWarnings("rawtypes")
public interface LoaderConfig {

    /**
     * Checks whether the config contains a loader that supports the given {@code element}.
     *
     * @param element element
     * @return true if the config contains suitable loader, of false otherwise
     */
    boolean supports(Element element);

    /**
     * @param element element to load
     * @return {@link ComponentLoader} instance
     */
    Class<? extends ComponentLoader> getLoader(Element element);

    /**
     * @param root fragment's root element
     * @return loader class for fragment or {@code null} if config does not support given {@code root}
     */
    /*@SuppressWarnings("rawtypes")
    @Nullable
    Class<? extends ComponentLoader> getFragmentLoader(Element root);*/

    /**
     * @param root window's root element
     * @return loader class for window or {@code null} if config does not support given {@code root}
     */
    @Nullable
    Class<? extends ComponentLoader> getScreenLoader(Element root);
}
