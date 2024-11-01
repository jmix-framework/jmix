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

package io.jmix.flowui.kit.action;

/**
 * Set of variants applicable to {@link Action}. This can be used to assign
 * a special visual style to a component that represents the action, for example,
 * by setting a suitable {@link com.vaadin.flow.component.shared.ThemeVariant}.
 */
public enum ActionVariant {

    /**
     * The default action variant.
     */
    DEFAULT,

    /**
     * A variant of important actions or ones you want to highlight.
     */
    PRIMARY,

    /**
     * A variant of actions related to dangerous operations, such as removing data.
     */
    DANGER,

    /**
     * A variant of actions related to safe operations or simply providing a different style to {@link #DEFAULT}.
     */
    SUCCESS
}
