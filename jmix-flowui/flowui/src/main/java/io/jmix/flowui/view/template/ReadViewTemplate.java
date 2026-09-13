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

package io.jmix.flowui.view.template;

import io.jmix.core.annotation.Experimental;
import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the framework generates a read view for the annotated entity at runtime.
 * <p>
 * The generated view shows the entity and cannot edit it. It is registered as the entity's primary read
 * view, so read view resolution finds it: the read view navigator, a {@code list_read} action and an
 * {@code entity_read} action all open it. Its route requires an entity id, so the view always opens for an
 * existing instance. Note that the generated list view carries no {@code list_read} action of its own.
 */
@Experimental
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MetaAnnotation
public @interface ReadViewTemplate {

    /**
     * Resource path to the Freemarker XML descriptor template.
     */
    String path() default "io/jmix/flowui/view/template/read-view.ftl";

    /**
     * JSON object with additional template parameters.
     */
    String templateParams() default "";

    /**
     * View id. If empty, the framework uses {@code <entityName>.read}.
     */
    String viewId() default "";

    /**
     * Base view route. The framework always appends {@code /:id/read}.
     * If empty, the framework derives the base route from {@code viewId}.
     */
    String viewRoute() default "";

    /**
     * View title. If empty, the framework uses {@code <entityName>}.
     * <p>
     * If the value starts with {@code msg://}, it is treated as a message reference and resolved through
     * the {@code Messages} bean. Both the full format {@code msg://group/message_id} and the brief format
     * {@code msg://message_id} (resolved against the entity package) are supported.
     */
    String viewTitle() default "";

    /**
     * Shown entity container id used by the generated controller. As with
     * {@link DetailViewTemplate#editedEntityContainerId()}, the stock template declares the container
     * under the default id, so a different value goes together with a custom {@link #path()}.
     */
    String readEntityContainerId() default "entityDc";
}
