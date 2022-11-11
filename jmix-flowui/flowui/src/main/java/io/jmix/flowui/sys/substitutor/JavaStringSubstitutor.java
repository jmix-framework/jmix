/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys.substitutor;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Substitutes variables of a within a string by values. The default definition of a variable is
 * <code>${variableName}</code>.
 */
@Component("flowui_StringSubstitutor")
public class JavaStringSubstitutor implements StringSubstitutor {

    @Override
    public String substitute(String source, Map<String, Object> valuesMap) {
        org.apache.commons.text.StringSubstitutor substitutor = new org.apache.commons.text.StringSubstitutor(valuesMap);
        return substitutor.replace(source);
    }
}
