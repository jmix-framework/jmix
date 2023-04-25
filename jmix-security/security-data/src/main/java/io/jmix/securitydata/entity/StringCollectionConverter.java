/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securitydata.entity;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Converter
public class StringCollectionConverter implements AttributeConverter<Set<String>, String> {

    public static final String REGEX = "\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"";
    private static final Pattern pattern = Pattern.compile(REGEX);

    @Override
    public String convertToDatabaseColumn(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .map(s -> s.replaceAll("\"", Matcher.quoteReplacement("\\\"")))
                .collect(Collectors.joining("\", \"", "\"", "\""));
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return Collections.emptySet();
        }
        Matcher matcher = pattern.matcher(dbData);
        Set<String> result = new HashSet<>();
        while (matcher.find()) {
            String code = matcher.group();
            code = code.substring(1, code.length() - 1);
            code = code.replaceAll(Matcher.quoteReplacement("\\\""), "\"");
            result.add(code);
        }
        return result;
    }
}
