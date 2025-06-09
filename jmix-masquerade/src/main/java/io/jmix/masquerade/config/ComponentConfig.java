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

package io.jmix.masquerade.config;

import io.jmix.masquerade.JComponents;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.function.Function;

/**
 * SPI interface for {@link JComponents} factory. Implement this interface in your project and create
 * {@code META-INF/services/io.jmix.masquerade.config.ComponentConfig} in your classpath with FQN
 * of your implementation.
 * <p>
 * For example:
 * <pre>{@code
 * public class CustomComponentConfig implements ComponentConfig {
 *
 *     @Override
 *     public Map<Class<?>, Function<By, ?>> getComponents() {
 *         return Map.of(Untyped.class, Untyped::new);
 *     }
 * }</code></pre>
 * }</pre>
 */
public interface ComponentConfig {

    /**
     * @return {@link Map} of components class to the components constructor with the {@link By} parameter
     */
    Map<Class<?>, Function<By, ?>> getComponents();
}
