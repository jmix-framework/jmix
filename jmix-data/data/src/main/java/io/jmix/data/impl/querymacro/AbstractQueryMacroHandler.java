/*
 * Copyright 2019 Haulmont.
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


import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.impl.QueryMacroHandler;

import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractQueryMacroHandler implements QueryMacroHandler {

    protected int count;
    protected final Pattern macroPattern;
    protected Map<String, Class> expandedParamTypes;

    protected CurrentAuthentication currentAuthentication;

    protected AbstractQueryMacroHandler(Pattern macroPattern) {
        this.macroPattern = macroPattern;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    public String expandMacro(String queryString) {
        count = 0;
        Matcher matcher = macroPattern.matcher(queryString);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, doExpand(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    protected abstract String doExpand(String macro);

    @Override
    public void setExpandedParamTypes(Map<String, Class> expandedParamTypes) {
        this.expandedParamTypes = expandedParamTypes;
    }

    @Nullable
    protected TimeZone getTimeZoneFromArgs(String[] args, int pos) {
        if (pos < args.length) {
            if ("USER_TIMEZONE".equalsIgnoreCase(args[pos].trim())) {
                return currentAuthentication.getTimeZone();
            }
        }
        return null;
    }
}
