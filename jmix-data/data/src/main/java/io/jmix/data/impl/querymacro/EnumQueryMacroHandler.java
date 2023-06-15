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


import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@Component("data_EnumQueryMacroHandler")
@Scope("prototype")
public class EnumQueryMacroHandler extends AbstractQueryMacroHandler {

    protected static final Pattern MACRO_PATTERN = Pattern.compile("@enum\\s*\\(([^)]+)\\)");

    public EnumQueryMacroHandler() {
        super(MACRO_PATTERN);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String doExpand(String enumString) {
        int idx = enumString.lastIndexOf('.');
        String className = enumString.substring(0, idx);
        String valueName = enumString.substring(idx + 1);
        Class<Enum> aClass;
        try {
            aClass = (Class<Enum>) ReflectionHelper.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error expanding JPQL macro", e);
        }
        for (Enum anEnum : aClass.getEnumConstants()) {
            if (valueName.equals(anEnum.name())) {
                if (anEnum instanceof EnumClass) {
                    Object id = ((EnumClass) anEnum).getId();
                    if (id instanceof String) {
                        return "'" + id + "'";
                    } else {
                        return id.toString();
                    }
                } else {
                    return String.valueOf(anEnum.ordinal());
                }
            }
        }
        throw new RuntimeException(String.format("Error expanding JPQL macro: enum %s is not found", enumString));
    }

    @Override
    public void setQueryParams(Map<String, Object> namedParameters) {
    }

    @Override
    public Map<String, Object> getParams() {
        return Collections.emptyMap();
    }

    @Override
    public String replaceQueryParams(String queryString, Map<String, Object> params) {
        return queryString;
    }
}