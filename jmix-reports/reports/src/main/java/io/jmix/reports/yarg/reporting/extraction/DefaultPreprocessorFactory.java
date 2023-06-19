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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default preprocessor factory implementation
 */
public class DefaultPreprocessorFactory implements PreprocessorFactory {
    protected QueryLoaderPreprocessor defaultPreprocessor;
    protected Map<String, QueryLoaderPreprocessor> preprocessorMap = new ConcurrentHashMap<>();

    public DefaultPreprocessorFactory() {
        this((query, params, consumer)-> consumer.apply(query, params));
    }

    public DefaultPreprocessorFactory(QueryLoaderPreprocessor defaultPreprocessor) {
        this.defaultPreprocessor = defaultPreprocessor;
    }

    @Override
    public void register(String loaderType, QueryLoaderPreprocessor preprocessor) {
        preprocessorMap.put(loaderType, preprocessor);
    }

    @Override
    public QueryLoaderPreprocessor processorBy(String loaderType) {
        return preprocessorMap.getOrDefault(loaderType, defaultPreprocessor);
    }

    public void setPreprocessors(Map<String, QueryLoaderPreprocessor> preprocessors) {
        checkNotNull(preprocessors);

        preprocessorMap = preprocessors;
    }

    public Map<String, QueryLoaderPreprocessor> getPreprocessors() {
        return Collections.unmodifiableMap(preprocessorMap);
    }
}
