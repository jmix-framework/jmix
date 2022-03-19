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

package com.haulmont.cuba.core.global;

import io.jmix.core.MessageTools;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Locale;

public class CubaMessageTools extends MessageTools {

    public static final String MAIN_MARK = "mainMsg://";

    @Override
    public boolean isMessageKey(@Nullable String message) {
        return StringUtils.isNotEmpty(message)
                && (message.startsWith(MARK) || message.startsWith(MAIN_MARK));
    }

    @Override
    public String loadString(@Nullable String group, @Nullable String ref, @Nullable Locale locale) {
        if (ref != null && ref.startsWith(MAIN_MARK)) {
            String path = ref.substring(10);

            if (locale == null) {
                return ((Messages) messages).getMainMessage(path);
            } else {
                return ((Messages) messages).getMainMessage(path, locale);
            }
        }

        return super.loadString(group, ref, locale);
    }
}
