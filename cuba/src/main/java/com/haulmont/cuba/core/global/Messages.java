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

package com.haulmont.cuba.core.global;

import io.jmix.core.MessageTools;

import java.util.Locale;

/**
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.Messages}.
 */
@Deprecated
public interface Messages extends io.jmix.core.Messages {

    /**
     * Convenient access to {@link MessageTools} bean.
     * @return  MessageTools instance
     */
    MessageTools getTools();

    /**
     * Returns localized message using main message pack.<br>
     * Locale is determined by the current user session.
     *
     * @param key message key
     * @return localized message or the key if the message not found
     */
    String getMainMessage(String key);

    /**
     * Returns localized message
     *
     * @param key    message key
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    String getMainMessage(String key, Locale locale);

    /**
     * Get localized message from main message pack and use it as a format string for parameters provided.<br>
     * Locale is determined by the current user session.
     *
     * @param key    message key
     * @param params parameter values
     * @return formatted string or the key in case of IllegalFormatException
     */
    String formatMainMessage(String key, Object... params);

}
