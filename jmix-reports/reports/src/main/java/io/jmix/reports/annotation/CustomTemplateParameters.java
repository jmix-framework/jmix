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

package io.jmix.reports.annotation;

import io.jmix.reports.entity.CustomTemplateDefinedBy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines parameters of the custom report template as part of the {@link TemplateDef} definition.
 * Custom template can be used to provide custom implementation for generation of the output document
 * based on band data and value formats.
 * <br/>
 * Model object is {@link io.jmix.reports.entity.ReportTemplate}.
 *
 * @see ReportDef
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTemplateParameters {

    /**
     * @return true if a custom template implementation should be used
     */
    boolean enabled() default false;

    /**
     * Specify how custom template is invoked.
     * Note: SCRIPT and CLASS are not supported here, use DELEGATE instead.
     */
    CustomTemplateDefinedBy definedBy() default CustomTemplateDefinedBy.DELEGATE;

    /**
     * Script that accepts parameters map and returns a URL that be called for to generate the output.
     */
    String urlScript() default "";
}
