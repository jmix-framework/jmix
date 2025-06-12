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

package io.jmix.flowui.kit.meta;

/**
 * Enum representing the strategy for evaluating required dependencies in Studio.
 * <p>
 * The strategy determines how the dependencies specified in annotations like {@code StudioUiKit}
 * are treated when determining whether a component or feature can be used in Studio.
 * <ul>
 *     <li>{@code OR}: Any one of the specified dependencies must be present.</li>
 *     <li>{@code AND}: All of the specified dependencies must be present.</li>
 * </ul>
 * This enum is typically used in annotations to specify the criteria under which certain
 * functionality should be available based on dependencies.
 */
public enum RequiredDependenciesStrategy {
    OR, AND
}
