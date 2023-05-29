/*
 * Copyright 2014 Haulmont
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

import io.jmix.reports.yarg.loaders.impl.json.JsonMap;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads data from json string
 * Uses JsonPath to access necessary parts of json object
 * Example:
 * JSON:
 * { "store": {
 * "book": [
 * { "category": "reference",
 * "author": "Nigel Rees",
 * "title": "Sayings of the Century",
 * "price": 8.95
 * },
 * { "category": "fiction",
 * "author": "Evelyn Waugh",
 * "title": "Sword of Honour",
 * "price": 12.99,
 * "isbn": "0-553-21311-3"
 * }
 * ],
 * "bicycle": {
 * "color": "red",
 * "price": 19.95
 * }
 * }
 * }
 * Query string:
 * parameter=param1 $.store.book[*]
 * We get json string from parameter param1 and select all "book" objects from the "store" object
 */
public class JsonDataLoader extends AbstractDataLoader {
    protected Pattern parameterPattern = Pattern.compile("parameter=([A-z0-9_]+)");

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParams) {
        Map<String, Object> currentParams = copyParameters(reportParams);

        Matcher matcher = parameterPattern.matcher(reportQuery.getScript());
        String parameterName = getParameterName(matcher);

        addParentBandDataToParametersRecursively(parentBand, currentParams);

        List<Map<String, Object>> result;

        if (parameterName != null) {
            Object parameterValue = currentParams.get(parameterName);
            if (parameterValue != null && StringUtils.isNotBlank(parameterValue.toString())) {
                result = loadDataFromScript(reportQuery, currentParams, matcher, parameterValue);
            } else {
                return Collections.emptyList();
            }
        } else {
            throw new DataLoadingException(String.format("Query string doesn't contain link to parameter. " +
                    "Script [%s]", reportQuery.getScript()));
        }

        return result;
    }

    protected List<Map<String, Object>> loadDataFromScript(ReportQuery reportQuery, Map<String, Object> currentParams,
                                                           Matcher matcher, Object parameterValue) {
        List<Map<String, Object>> result;
        String json = parameterValue.toString();
        String script = matcher.replaceAll("");

        if (StringUtils.isBlank(script)) {
            throw new DataLoadingException(
                    String.format("The script doesn't contain json path expression. " +
                            "Script [%s]", reportQuery.getScript()));
        }

        matcher = AbstractDbDataLoader.COMMON_PARAM_PATTERN.matcher(script);
        while (matcher.find()) {
            String parameter = matcher.group(1);
            script = matcher.replaceAll(String.valueOf(currentParams.get(parameter)));
        }

        result = extractScriptResult(json, script, reportQuery);
        return result;
    }

    protected List<Map<String, Object>> extractScriptResult(String jsonData, String jsonPathScript, ReportQuery reportQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Object scriptResult = JsonPath.read(jsonData, jsonPathScript);
            parseScriptResult(result, jsonPathScript, scriptResult);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            return Collections.emptyList();
        } catch (Throwable e) {
            throw new DataLoadingException(
                    String.format("An error occurred while loading data with script [%s]", reportQuery.getScript()), e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected void parseScriptResult(List<Map<String, Object>> result, String script, Object scriptResult) {
        if (scriptResult instanceof List) {//JSONArray is also list
            List theList = (List) scriptResult;
            if (!theList.isEmpty()) {
                Object listObject = theList.get(0);
                if (listObject instanceof Map) {
                    for (Object object : theList) {
                        result.add(createMap((Map) object));
                    }
                } else {
                    throw new DataLoadingException(
                            String.format("The list collected with script does not contain objects. " +
                                    "It contains %s instead. " +
                                    "Script [%s]", listObject, script));
                }
            }
        } else if (scriptResult instanceof Map) {
            result.add(createMap((Map) scriptResult));
        } else {
            throw new DataLoadingException(
                    String.format("The script collects neither object nor list of objects. " +
                            "Script [%s]", script));
        }
    }

    protected String getParameterName(Matcher matcher) {
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    protected Map<String, Object> createMap(Map jsonObject) {
        return new JsonMap(jsonObject);
    }

    protected Map<String, Object> copyParameters(Map<String, Object> parametersToCopy) {
        Map<String, Object> copyParams = new HashMap<>();
        if (parametersToCopy != null) {
            copyParams.putAll(parametersToCopy);
        }
        return copyParams;
    }

    protected void addParentBandDataToParametersRecursively(BandData parentBand, Map<String, Object> currentParams) {
        while (parentBand != null) {
            addParentBandDataToParameters(parentBand, currentParams);
            parentBand = parentBand.getParentBand();
        }
    }

    protected void addParentBandDataToParameters(BandData parentBand, Map<String, Object> currentParams) {
        if (parentBand != null) {
            String parentBandName = parentBand.getName();

            for (Map.Entry<String, Object> entry : parentBand.getData().entrySet()) {
                currentParams.put(parentBandName + "." + entry.getKey(), entry.getValue());
            }
        }
    }
}
