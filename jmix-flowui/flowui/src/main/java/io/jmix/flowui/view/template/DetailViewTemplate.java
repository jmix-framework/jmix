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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the framework should generate a detail view for the annotated entity at runtime.
 */
@Experimental
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MetaAnnotation
public @interface DetailViewTemplate {

    /**
     * Resource path to the Freemarker XML descriptor template.
     */
    String path() default "io/jmix/flowui/view/template/detail-template.ftl";

    /**
     * JSON object with additional template parameters.
     */
    String templateParams() default "";

    /**
     * Parent menu item id. If empty, the framework does not create a menu item.
     */
    String parentMenu() default "";

    /**
     * View id. If empty, the framework uses {@code <entityName>.detail}.
     */
    String viewId() default "";

    /**
     * Base view route. The framework always appends {@code /:id}.
     * If empty, the framework derives the base route from {@code viewId}.
     */
    String viewRoute() default "";

    /**
     * View title. If empty, the framework uses {@code <entityName>}.
     */
    String viewTitle() default "";
}
