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

package io.jmix.ui.screen;

import com.vaadin.ui.Component;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.HasDependencies;
import io.jmix.ui.component.Window;
import io.jmix.ui.widget.JmixWindowVerticalLayout;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A class that facilitates adding dependencies such as
 * CSS, JavaScript, HTML to the current page for screens and fragments.
 */
@ParametersAreNonnullByDefault
public final class ScreenDependencyUtils {

    /**
     * Returns a list of dependencies previously added to the given frame owner.
     *
     * @param frameOwner a frame owner from which the dependency list is obtained
     * @return a list of dependencies
     */
    public static List<HasDependencies.ClientDependency> getScreenDependencies(FrameOwner frameOwner) {
        Optional<JmixWindowVerticalLayout> layoutOptional = findWindowVerticalLayout(frameOwner);
        return layoutOptional.isPresent() ? layoutOptional.get().getDependencies() : Collections.emptyList();
    }

    /**
     * Sets a list of dependencies to the given {@code frameOwner}.
     * Each dependency represented with a {@link HasDependencies.ClientDependency} object which path corresponds to one of the sources:
     *
     * <ul>
     * <li>WebJar resource - starts with {@code webjar://}</li>
     * <li>VAADIN directory - starts with {@code vaadin://}</li>
     * <li>Web resource - starts with {@code http://} or {@code https://}</li>
     * </ul>
     *
     * @param frameOwner   a frame owner to which dependencies are added
     * @param dependencies dependencies to set
     */
    public static void setScreenDependencies(FrameOwner frameOwner, List<HasDependencies.ClientDependency> dependencies) {
        findWindowVerticalLayout(frameOwner).ifPresent(layout ->
                layout.setDependencies(dependencies));
    }

    /**
     * Adds dependency paths to the given {@code frameOwner}.
     * Each path corresponds to one of the sources:
     *
     * <ul>
     * <li>WebJar resource - starts with {@code webjar://}</li>
     * <li>VAADIN directory - starts with {@code vaadin://}</li>
     * <li>Web resource - starts with {@code http://} or {@code https://}</li>
     * </ul>
     *
     * @param frameOwner   a frame owner to which dependencies are added
     * @param dependencies dependencies to add
     */
    public static void addScreenDependencies(FrameOwner frameOwner, String... dependencies) {
        findWindowVerticalLayout(frameOwner).ifPresent(layout ->
                layout.addDependencies(dependencies));
    }

    /**
     * Adds a dependency to the given {@code frameOwner}. Path corresponds to one of the sources:
     *
     * <ul>
     * <li>WebJar resource - starts with {@code webjar://}</li>
     * <li>VAADIN directory - starts with {@code vaadin://}</li>
     * <li>Web resource - starts with {@code http://} or {@code https://}</li>
     * </ul>
     *
     * @param frameOwner a frame owner to which a dependency is added
     * @param path       a dependency path
     * @param type       a dependency type
     */
    public static void addScreenDependency(FrameOwner frameOwner, String path, Dependency.Type type) {
        findWindowVerticalLayout(frameOwner).ifPresent(layout ->
                layout.addDependency(path, type));
    }

    protected static Optional<JmixWindowVerticalLayout> findWindowVerticalLayout(FrameOwner frameOwner) {
        Window window = UiControllerUtils.getScreen(frameOwner).getWindow();
        Component vComponent = window.unwrap(Component.class);
        if (vComponent instanceof JmixWindowVerticalLayout) {
            return Optional.of(((JmixWindowVerticalLayout) vComponent));
        }

        return Optional.empty();
    }
}
