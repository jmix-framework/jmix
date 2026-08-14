/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview;

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Studio-side services available to {@link StudioPreviewComponentLoader preview loaders}.
 * Implemented by Studio via {@link java.lang.reflect.Proxy} over the preview classloader,
 * therefore: JDK-only signatures, every method may return {@code null}, implementations never throw.
 */
public interface StudioPreviewEnvironment {

    StudioPreviewEnvironment NOOP = new StudioPreviewEnvironment() {
        @Nullable
        @Override
        public String resolveMessage(String messageKey) {
            return null;
        }

        @Nullable
        @Override
        public String propertyCaption(@Nullable String dataContainerId, @Nullable String metaClass,
                                      String propertyPath) {
            return null;
        }
    };

    /**
     * Resolves a localized message reference (e.g. {@code msg://key}) using the IDE project messages.
     */
    @Nullable
    String resolveMessage(String messageKey);

    /**
     * Resolves the localized caption of an entity property, e.g. for a dataGrid column header.
     */
    @Nullable
    String propertyCaption(@Nullable String dataContainerId, @Nullable String metaClass, String propertyPath);

    /**
     * The calling Studio's plugin version, e.g. {@code "2.8.1"}, for loaders that must branch on it.
     * {@code null} when Studio is older than this method (its proxy returns {@code null} for
     * anything it doesn't implement) or when no environment was supplied at all.
     * <p>
     * {@code default} on purpose: every method added here must stay optional so implementations —
     * Studio's proxy included — keep compiling and behaving as "not supported" until they opt in.
     */
    @Nullable
    default String studioVersion() {
        return null;
    }

    /**
     * Resolves a project static resource (Spring conventions: resource root, {@code META-INF/resources},
     * {@code static}, {@code public}) to a {@code data:<mime>;base64,...} URL usable as an image/svg src.
     */
    @Nullable
    default String resolveStaticResource(String path) {
        return null;
    }

    /**
     * The descriptor XML text of the fragment declared by the given controller class,
     * for rendering fragment content inline.
     */
    @Nullable
    default String resolveFragmentDescriptor(String fragmentClassFqn) {
        return null;
    }

    /**
     * The project's main menu (menu.xml) as a JSON array, recursive:
     * {@code [{"id": "...", "title": "<localized>", "icon": "<icon attr or null>", "items": [...]}]}.
     */
    @Nullable
    default String mainMenuItems() {
        return null;
    }

    /**
     * Attributes of the entity behind a data container / meta class as a JSON array:
     * {@code [{"name": "...", "caption": "<localized>", "type": "<java or enum fqn>"}]}.
     * Same resolution rules as {@link #propertyCaption}.
     */
    @Nullable
    default String entityProperties(@Nullable String dataContainerId, @Nullable String metaClass) {
        return null;
    }

    /**
     * A single application property value ({@code application.properties} merged the way Studio sees it).
     */
    @Nullable
    default String applicationProperty(String key) {
        return null;
    }

    /**
     * All application properties as a key-value map. Prefer this over repeated
     * {@link #applicationProperty} calls when several keys are needed — every environment call
     * crosses the classloader boundary into a Studio read action.
     */
    @Nullable
    default Map<String, String> applicationProperties() {
        return null;
    }

    /**
     * Constants of a project enum class as a JSON array:
     * {@code [{"name": "...", "caption": "<localized or natural>"}]}.
     * Project enums are not on the preview classloader, so only Studio can enumerate them.
     */
    @Nullable
    default String enumItems(String enumClassFqn) {
        return null;
    }

    /**
     * The localized title of a view identified by view id or controller class FQN.
     */
    @Nullable
    default String viewTitle(String viewIdOrClassFqn) {
        return null;
    }

    /**
     * SVG of an icon from a project custom icon set (an {@code IconFactory} + {@code @JsModule}
     * iconset invisible to the preview classloader), as a {@code data:image/svg+xml;base64,...} URL.
     * {@code null} for standard/unknown icons.
     */
    @Nullable
    default String resolveIconSvg(String icon) {
        return null;
    }
}
