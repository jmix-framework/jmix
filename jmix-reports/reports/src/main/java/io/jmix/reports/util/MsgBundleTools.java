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

package io.jmix.reports.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.jmix.core.LocaleResolver;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Internal
@Component("report_MsgBundleTools")
public class MsgBundleTools {
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Nullable
    public String getLocalizedValue(String msgBundle, String defaultValue) {
        String key = LocaleResolver.localeToString(currentAuthentication.getLocale());
        Map<String, String> localizedValues = getMsgBundleValues(msgBundle);
        if (localizedValues.containsKey(key)) {
            return localizedValues.get(key);
        }
        return defaultValue;
    }

    protected Map<String, String> getMsgBundleValues(@Nullable String localeBundle) {
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
}