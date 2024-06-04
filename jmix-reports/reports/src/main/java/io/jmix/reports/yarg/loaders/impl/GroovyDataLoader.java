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
package io.jmix.reports.yarg.loaders.impl;

import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.reports.yarg.util.groovy.Scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads data using groovy script
 * Script should return list of maps
 *
 * Example:
 * return [['a':123, 'b':321], ['a':456, 'b':params['param1']]]
 */
public class GroovyDataLoader implements ReportDataLoader {
    private Scripting scripting;

    public GroovyDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        try {
            String script = reportQuery.getScript();
            Map<String, Object> scriptParams = new HashMap<String, Object>();
            scriptParams.put("reportQuery", reportQuery);
            scriptParams.put("parentBand", parentBand);
            scriptParams.put("params", params);
            return scripting.evaluateGroovy(script, scriptParams);
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        }
    }
}
