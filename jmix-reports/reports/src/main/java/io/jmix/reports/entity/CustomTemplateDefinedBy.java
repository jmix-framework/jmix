/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.yarg.formatters.CustomReport;

/**
 * Determines how a custom report generation is defined and invoked.
 * Used by custom report templates.
 * @see io.jmix.reports.annotation.CustomTemplateParameters
 * @see ReportTemplate
 */
public enum CustomTemplateDefinedBy implements EnumClass<Integer> {
    /**
     * Custom template renderer is defined by a class located on the classpath.
     * The class must implement {@link CustomReport}, and must have a zero-argument constructor.
     * The class is lazily loaded, and its instance is created at the moment of report generation.
     */
    CLASS(100),

    /**
     * Custom template renderer is implemented as a Groovy script.
     */
    SCRIPT(200),

    /**
     * Renderer is implemented as a remote HTTP service.
     * Calling the renderer is performed in two stages:
     * <ol>
     * <li>Groovy script is called to determine the URL, including query parameters</li>
     * <li>command-line <code>curl</code> utility is used to invoke the remote service</li>
     * </ol>
     * <br/>
     * CURL-related properties are applicable here.
     *
     * @see ReportsProperties#getCurlPath()
     * @see ReportsProperties#getCurlParams()
     * @see ReportsProperties#getCurlTimeout()
     */
    URL(300),

    /**
     * Object implementing {@link CustomReport} provided by the user.
     */
    DELEGATE(400);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    CustomTemplateDefinedBy(Integer id) {
        this.id = id;
    }

    public static Integer getId(CustomTemplateDefinedBy definedBy) {
        return definedBy != null ? definedBy.getId() : null;
    }

    public static CustomTemplateDefinedBy fromId(Integer id) {
        for (CustomTemplateDefinedBy type : CustomTemplateDefinedBy.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}