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

package io.jmix.reports.libintegration;

import io.jmix.core.Resources;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.exception.ValidationException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.reports.yarg.util.groovy.Scripting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class JmixGroovyDataLoader implements ReportDataLoader {

    protected Scripting scripting;

    @Autowired
    protected Resources resources;

    @Autowired
    protected GroovyScriptParametersProvider groovyScriptParametersProvider;

    @Autowired
    public JmixGroovyDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        try {
            String script = reportQuery.getScript();
            Map<String, Object> scriptParams = groovyScriptParametersProvider.getParametersForDatasetParameters(
                    reportQuery, parentBand, params);

            script = StringUtils.trim(script);
            if (script.endsWith(".groovy")) {
                script = resources.getResourceAsString(script);
            }
            return scripting.evaluateGroovy(script, scriptParams);
        } catch (ValidationException e) {
            throw e;
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]",
                    reportQuery.getName()), e);
        }
    }
}