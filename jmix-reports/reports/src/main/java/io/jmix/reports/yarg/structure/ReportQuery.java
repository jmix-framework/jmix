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
import java.util.Map;

/**
 * This interface describes certain query which load some data.
 * It might be not only SQL or JPQL query but also Groovy script or smth like this
 */
public interface ReportQuery extends Serializable {
    String getName();

    /**
     * Sql, groovy or other script which describes logic of data loading
     */
    String getScript();

    String getLinkParameterName();

    /**
     * @return loader code.
     * See io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory implementations and io.jmix.reports.yarg.loaders.factory.DefaultLoaderFactory for default values.
     */
    String getLoaderType();

    Boolean getProcessTemplate();

    Map<String, Object> getAdditionalParams();
}