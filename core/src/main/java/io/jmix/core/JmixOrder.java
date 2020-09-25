/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core;

/**
 * Defines constants for higher and lower precedence for {@link org.springframework.core.Ordered} or
 * annotated with {@link org.springframework.core.annotation.Order} beans of the Jmix modules.
 */
public class JmixOrder {

    /**
     * Defines the highest precedence for {@link org.springframework.core.Ordered} or
     * annotated with {@link org.springframework.core.annotation.Order} beans of the Jmix modules.
     */
    public static final int HIGHEST_PRECEDENCE = 100;

    /**
     * Defines the lowest precedence for {@link org.springframework.core.Ordered} or
     * annotated with {@link org.springframework.core.annotation.Order} beans of the Jmix modules.
     */
    public static final int LOWEST_PRECEDENCE = 1000;
}
