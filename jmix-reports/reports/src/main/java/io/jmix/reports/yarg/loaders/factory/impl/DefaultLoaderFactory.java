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
package io.jmix.reports.yarg.loaders.factory.impl;

import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.exception.UnsupportedLoaderException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.HashMap;
import java.util.Map;

public class DefaultLoaderFactory implements ReportLoaderFactory {
    public static final String GROOVY_DATA_LOADER = "groovy";
    public static final String SQL_DATA_LOADER = "sql";
    public static final String JSON_DATA_LOADER = "json";

    protected Map<String, ReportDataLoader> dataLoaders = new HashMap<String, ReportDataLoader>();

    public DefaultLoaderFactory setDataLoaders(Map<String, ReportDataLoader> dataLoaders) {
        this.dataLoaders.putAll(dataLoaders);
        return this;
    }

    public Map<String, ReportDataLoader> getDataLoaders() {
        return dataLoaders;
    }

    public DefaultLoaderFactory setGroovyDataLoader(ReportDataLoader dataLoader) {
        return registerDataLoader(GROOVY_DATA_LOADER, dataLoader);
    }

    public DefaultLoaderFactory setSqlDataLoader(ReportDataLoader dataLoader) {
        return registerDataLoader(SQL_DATA_LOADER, dataLoader);
    }

    public DefaultLoaderFactory setJsonDataLoader(ReportDataLoader dataLoader) {
        return registerDataLoader(JSON_DATA_LOADER, dataLoader);
    }

    public DefaultLoaderFactory registerDataLoader(String key, ReportDataLoader dataLoader) {
        dataLoaders.put(key, dataLoader);
        return this;
    }

    @Override
    public ReportDataLoader createDataLoader(String loaderType) {
        ReportDataLoader dataLoader = dataLoaders.get(loaderType);
        if (dataLoader == null) {
            throw new UnsupportedLoaderException(String.format("Unsupported loader type [%s]", loaderType));
        } else {
            return dataLoader;
        }
    }
}