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

package io.jmix.dynattr;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.jmix.core.LocaleResolver;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Internal
@Component("dynat_MsgBundleTools")
public final class MsgBundleTools {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public String getLocalizedValue(String msgBundle, String defaultValue) {
        String key = LocaleResolver.localeToString(currentAuthentication.getLocale());
        Map<String, String> localizedValues = getMsgBundleValues(msgBundle);
        if (localizedValues.containsKey(key)) {
            return localizedValues.get(key);
        }
        return defaultValue;
    }

    public String getLocalizedEnumeration(String msgBundle, String enumeration) {
        Map<String, String> localizedValues = getMsgBundleValues(msgBundle);
        if (localizedValues.isEmpty()) {
            return enumeration;
        } else {
            String key = LocaleResolver.localeToString(currentAuthentication.getLocale());
            List<String> newValues = new ArrayList<>();
            for (String value : Splitter.on(",").omitEmptyStrings().split(enumeration)) {
                newValues.add(localizedValues.getOrDefault(key + "/" + value, value));
            }
            return Joiner.on(",").join(newValues);
        }
    }

    public Map<String, String> getMsgBundleValues(String localeBundle) {
        Map<String, String> result = Collections.emptyMap();
        if (!Strings.isNullOrEmpty(localeBundle)) {
            try {
                Properties properties = new Properties();
                properties.load(new StringReader(localeBundle));
                result = Maps.fromProperties(properties);
            } catch (IOException e) {
            }
        }
        return result;
    }

    @Nullable
    public String getMsgBundle(Properties properties) {
        StringWriter writer = new StringWriter();
        String result = null;
        boolean written = false;
        try {
            properties.store(writer, "");
            written = true;
        } catch (IOException ignored) {
        }

        if (written) {
            StringBuffer buffer = writer.getBuffer();
            Pattern pattern = Pattern.compile("(?m)^#.*\\s\\s");
            result = pattern.matcher(buffer).replaceAll("");
        }
        return result;
    }

    @Nullable
    public String convertMsgBundleToEnumMsgBundle(String enumValue, String msgBundle) {
        Properties result = new Properties();
        Map<String, String> msgBundleValues = getMsgBundleValues(msgBundle);
        if (!msgBundleValues.isEmpty()) {
            for (Map.Entry<String, String> entry : msgBundleValues.entrySet()) {
                String key = entry.getKey();
                result.put(key + "/" + enumValue, entry.getValue());
            }
        }
        return getMsgBundle(result);
    }

    @Nullable
    public String convertEnumMsgBundleToMsgBundle(String enumMsgBundle) {
        Properties result = new Properties();
        Map<String, String> msgBundleValues = getMsgBundleValues(enumMsgBundle);
        if (!msgBundleValues.isEmpty()) {
            for (Map.Entry<String, String> entry : msgBundleValues.entrySet()) {
                String key = entry.getKey();
                key = key.substring(0, key.indexOf("/"));
                result.put(key, entry.getValue());
            }
        }
        return getMsgBundle(result);
    }

    public Map<String, String> getEnumMsgBundleValues(String localeBundle) {
        Map<String, String> result = new HashMap<>();
        if (!Strings.isNullOrEmpty(localeBundle)) {
            try {
                Properties properties = new Properties();
                properties.load(new StringReader(localeBundle));

                Map<String, Properties> enumMsgBundleValues = new HashMap<>();
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String entryKey = (String) entry.getKey();
                    String enumValue = entryKey.substring(entryKey.indexOf("/") + 1);

                    Properties enumProperties = enumMsgBundleValues.get(enumValue);
                    if (enumProperties == null) {
                        enumProperties = new Properties();
                        enumMsgBundleValues.put(enumValue, enumProperties);
                    }
                    enumProperties.put(entry.getKey(), entry.getValue());
                }

                result = enumMsgBundleValues.entrySet().stream()
                        .collect(Collectors.toMap(entry -> (String) entry.getKey(),
                                entry -> getMsgBundle(entry.getValue())));
            } catch (IOException e) {
            }
        }
        return result;
    }
}