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

package io.jmix.core;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Central interface to work with localized messages.
 */
public interface Messages {

    /**
     * Returns localized message.<br>
     * Locale is determined by the current user session.
     *
     * @param key message key
     * @return localized message or the key if the message not found
     */
    String getMessage(String key);

    /**
     * Returns localized message.
     *
     * @param key    message key
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    String getMessage(String key, Locale locale);

    /**
     * Returns localized message.<br>
     * Locale is determined by the current user session.
     *
     * @param caller determines the message group as class' package name
     * @param key    message key
     * @return localized message or the key if the message not found
     */
    String getMessage(Class caller, String key);

    /**
     * Returns localized message.
     *
     * @param caller determines the message group as class' package name
     * @param key    message key
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    String getMessage(Class caller, String key, Locale locale);

    /**
     * Returns localized message.<br>
     * Locale is determined by the current user session.
     *
     * @param caller enum determining the message group and key:
     *               <ul>
     *               <li>group - enum's package name</li>
     *               <li>key - enum's short class name (after last dot), plus dot, plus enum value</li>
     *               </ul>
     * @return localized message or the key if the message not found
     */
    String getMessage(Enum caller);

    /**
     * Returns localized message.
     *
     * @param caller enum determining the message group and key:
     *               <ul>
     *               <li>group - enum's package name</li>
     *               <li>key - enum's short class name (after last dot), plus dot, plus enum value</li>
     *               </ul>
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    String getMessage(Enum caller, Locale locale);

    /**
     * Returns localized message.
     * Locale is determined by the current user session.
     *
     * @param group     message group
     * @param key       message key
     * @return localized message or the key if the message not found
     */
    String getMessage(String group, String key);

    /**
     * Returns localized message.
     *
     * @param group  message group
     * @param key    message key
     * @param locale message locale
     * @return localized message or the key if the message not found
     */
    String getMessage(String group, String key, Locale locale);

    /**
     * Get localized message and use it as a format string for parameters provided.<br>
     * Locale is determined by the current user session.
     *
     * @param caller determines the message group as class' package name
     * @param key    message key
     * @param params parameter values
     * @return formatted string or the key in case of IllegalFormatException
     */
    String formatMessage(Class caller, String key, Object... params);

    /**
     * Get localized message and use it as a format string for parameters provided
     *
     * @param caller determines the message group as class' package name
     * @param key    message key
     * @param locale message locale
     * @param params parameter values
     * @return formatted string or the key in case of IllegalFormatException
     */
    String formatMessage(Class caller, String key, Locale locale, Object... params);

    /**
     * Get localized message and use it as a format string for parameters provided.<br>
     * Locale is determined by the current user session.
     *
     * @param group  message group
     * @param key    message key
     * @param params parameter values
     * @return formatted string or the key in case of IllegalFormatException
     */
    String formatMessage(String group, String key, Object... params);

    /**
     * Get localized message and use it as a format string for parameters provided
     *
     * @param group  message group
     * @param key    message key
     * @param locale message locale
     * @param params parameter values
     * @return formatted string or the key in case of IllegalFormatException
     */
    String formatMessage(String group, String key, Locale locale, Object... params);

    /**
     * Returns localized message or null if not found.
     *
     * @param key    message key
     * @param locale message locale. If null, current user locale is used.
     * @return localized message or null if the message not found
     */
    @Nullable
    String findMessage(String key, @Nullable Locale locale);

    /**
     * Returns localized message or null if not found.
     *
     * @param group  message group
     * @param key    message key
     * @param locale message locale. If null, current user locale is used.
     * @return localized message or null if the message not found
     */
    @Nullable
    String findMessage(String group, String key, @Nullable Locale locale);

    void clearCache();
}
