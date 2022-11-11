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

package io.jmix.flowui.action;

import io.jmix.flowui.Actions;
import io.jmix.flowui.kit.action.Action;

import java.lang.annotation.*;

/**
 * Indicates that {@link Action} can be created with {@link Actions}
 * factory and can be used in view XML descriptor.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActionType {

    String VALUE_ATTRIBUTE = "value";

    /**
     * @return id of action type
     */
    String value();
}