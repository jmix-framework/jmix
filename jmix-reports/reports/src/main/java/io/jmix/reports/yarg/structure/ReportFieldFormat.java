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

import java.io.Serializable;

/**
 * This interface describes a format for certain result field.
 */
public interface ReportFieldFormat extends Serializable {
    /**
     * @return formatted field name. Should also contain all parent band names.
     * Example: Band1.Band2.field1
     */
    String getName();

    /**
     * @return format string
     * Example: ##,# for decimals, dd-MM-yyyy for dates, etc.
     */
    String getFormat();

    /**
     * @return boolean <code>true</code> if the groovy script, otherwise <code>false</code>
     */
    Boolean isGroovyScript();
}