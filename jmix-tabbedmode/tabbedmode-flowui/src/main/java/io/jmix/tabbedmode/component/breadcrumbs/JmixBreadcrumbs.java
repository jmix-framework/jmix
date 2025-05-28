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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.core.common.util.Preconditions;

/**
 * Server-side component for the {@code <jmix-breadcrumbs>} element.
 * <p>
 * It's a container for {@link JmixBreadcrumb} components.
 */
@Tag("jmix-breadcrumbs")
@JsModule("./src/breadcrumbs/jmix-breadcrumbs.js")
public class JmixBreadcrumbs extends Component {

    public JmixBreadcrumbs() {
    }

    /**
     * Adds a breadcrumb to the container as the last child.
     *
     * @param breadcrumb the breadcrumb to add
     */
    public void add(JmixBreadcrumb breadcrumb) {
        Preconditions.checkNotNullArgument(breadcrumb);

        getElement().appendChild(breadcrumb.getElement());
    }

    /**
     * Removes a breadcrumb from the container.
     *
     * @param breadcrumb the breadcrumb to remove
     */
    public void remove(JmixBreadcrumb breadcrumb) {
        Preconditions.checkNotNullArgument(breadcrumb);

        getElement().removeChild(breadcrumb.getElement());
    }
}
