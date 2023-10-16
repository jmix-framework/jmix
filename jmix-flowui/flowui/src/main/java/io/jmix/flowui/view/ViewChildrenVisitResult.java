/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view;

import com.vaadin.flow.component.Component;
import org.springframework.lang.Nullable;

/**
 * ViewChildrenVisitResult data model class that stores results from walk through comonents tree from view
 * @see io.jmix.flowui.component.UiComponentUtils
 */
public class ViewChildrenVisitResult {
    private View<?> view;
    @Nullable
    private String componentId;
    private Component component;

    /**
     * @return target view, parent that contains found component result
     */
    public View<?> getView() {
        return view;
    }

    /**
     * @param view set view to store target view, parent that contains found component result
     */
    public void setView(View<?> view) {
        this.view = view;
    }

    /**
     * Returns found component id result inside tree elements in target view, also can be null because elements may not have own id
     * @return found component id result inside tree elements in target view
     */
    @Nullable
    public String getComponentId() {
        return componentId;
    }

    /**
     * @param componentId component id that was found inside view
     */
    public void setComponentId(@Nullable String componentId) {
        this.componentId = componentId;
    }

    /**
     * @return found component result inside tree elements in target view, always not null
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @param component component that was found inside view
     */
    public void setComponent(Component component) {
        this.component = component;
    }
}
