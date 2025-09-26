/*
 * Copyright 2025 Haulmont.
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

package io.jmix.data.impl.querymacro;

import io.jmix.core.DateTimeTransformations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A query macro handler that processes the `@dateBetween` macro. This macro is used to
 * filter date fields within a specified custom range. The `@dateBetween` macro expects arguments representing a
 * field name, a start parameter, an end parameter, and an optional time zone.
 */
@Component("data_DateBetweenQueryMacroHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateBetweenQueryMacroHandler extends AbstractQueryMacroHandler {

    protected static final Pattern MACRO_PATTERN = Pattern.compile("@dateBetween\\s*\\(([^)]+)\\)");
    protected static final Pattern QUERY_PARAM_PATTERN = Pattern.compile(":(\\w+)");

    protected DateTimeTransformations transformations;

    protected List<MacroArgs> macroArgs = new ArrayList<>();
    protected Map<String, Object> namedParameters;

    public DateBetweenQueryMacroHandler() {
        super(MACRO_PATTERN);
    }

    @Autowired
    public void setTransformations(DateTimeTransformations transformations) {
        this.transformations = transformations;
    }

    @Override
    public void setQueryParams(Map<String, Object> namedParameters) {
        this.namedParameters = namedParameters;
    }

    @Override
    protected String doExpand(String macro) {
        count++;
        String[] args = macro.split(",");
        if (args.length != 3 && args.length != 4) {
            throw new IllegalStateException("Invalid macro: " + macro);
        }

        String field = args[0];
        TimeZone timeZone = getTimeZoneFromArgs(args, 3);
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        String startParam = getParam(args, 1, timeZone);
        String endParam = getParam(args, 2, timeZone);

        return String.format("%s >= :%s and %s < :%s", field, startParam, field, endParam);
    }

    @Override
    public Map<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<>();
        for (MacroArgs macroArg : macroArgs) {
            TimeZone timeZone = macroArg.getTimeZone();
            String paramName = macroArg.getParamName();

            Object date = namedParameters.get(paramName);
            ZonedDateTime zonedDateTime = transformations.transformToZDT(date)
                    .withZoneSameInstant(timeZone.toZoneId());

            params.put(paramName, transformations.transformFromZDT(zonedDateTime, date.getClass()));
        }

        return params;
    }

    protected String getParam(String[] args, int index, TimeZone timeZone) {
        String arg = args[index].trim();

        if (!QUERY_PARAM_PATTERN.matcher(arg).find()) {
            throw new IllegalArgumentException("Invalid macro argument: " + arg);
        }

        arg = arg.substring(1);
        macroArgs.add(new MacroArgs(arg, timeZone));
        return arg;
    }

    @Override
    public String replaceQueryParams(String queryString, Map<String, Object> params) {
        return queryString;
    }
}
