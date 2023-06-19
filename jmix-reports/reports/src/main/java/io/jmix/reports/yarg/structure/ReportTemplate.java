/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.structure;

import io.jmix.reports.yarg.formatters.CustomReport;

import java.io.InputStream;
import java.io.Serializable;

/**
 * This interface describes report template document.
 */
public interface ReportTemplate extends Serializable {
    String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    String getCode();

    String getDocumentName();

    String getDocumentPath();

    /**
     * @return stream containing resulting document
     */
    InputStream getDocumentContent();

    /**
     * @return output type of for this template
     */
    ReportOutputType getOutputType();

    /**
     * @return name pattern for generating document. Example: ${Band1.FILE_NAME} or myDocument.doc
     */
    String getOutputNamePattern();

    /**
     * @return if it is groovy html template.
     */
    default boolean isGroovy() {
        return false;
    }

    /**
     * @return if report is defined by custom class.
     * In this case band data will be passed in io.jmix.reports.yarg.structure. ReportTemplate#getCustomReport() object and it will generate binary.
     */
    boolean isCustom();

    /**
     * @return implementation of custom report logic.
     */
    CustomReport getCustomReport();
}