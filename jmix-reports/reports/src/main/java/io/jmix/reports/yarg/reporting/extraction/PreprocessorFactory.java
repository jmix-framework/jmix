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

package io.jmix.reports.yarg.reporting.extraction;

import io.jmix.reports.yarg.loaders.QueryLoaderPreprocessor;

/**
 * <p>This interface implementation should holding relation between name of data loader type (<b>ex: sql</b>)
 * and custom params preprocessor
 * if relation not set, implementation should present default params preprocessor</p>
 * <p><b>ex:</b> {@code (query, params, consumer)-> consumer.apply(query, params) }</p>
 *
 * <p>The default implementation is <b>io.jmix.reports.yarg.reporting.extraction.DefaultPreprocessorFactory</b></p>
 */
public interface PreprocessorFactory {
    /**
     * Method for registering query preprocessing by loader type
     *
     * @param loaderType loader type ex: sql
     * @param preprocessor preprocessor implementation
     */
    void register(String loaderType, QueryLoaderPreprocessor preprocessor);

    /**
     * @param loaderType loader type ex: sql
     * @return preprocessor instance
     */
    QueryLoaderPreprocessor processorBy(String loaderType);
}
