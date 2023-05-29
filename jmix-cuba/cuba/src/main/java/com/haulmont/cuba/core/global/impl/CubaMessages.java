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

package com.haulmont.cuba.core.global.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.haulmont.cuba.core.global.UserSessionSource;
import io.jmix.core.LocaleResolver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * <code>Messages</code> implementation common for all tiers.
 */
@Component("cuba_CubaMessages")
public class CubaMessages {

    public static final String BUNDLE_NAME = "messages";
    public static final String EXT = ".properties";

    private static final Logger log = LoggerFactory.getLogger(CubaMessages.class);

    protected String confDir;

    protected Map<String, String> strCache = new ConcurrentHashMap<>();

    // Using ConcurrentHashMap instead of synchronized Set for better parallelism
    protected Map<String, String> notFoundCache = new ConcurrentHashMap<>();

    protected Cache<String, Properties> filePropertiesCache = CacheBuilder.newBuilder().build();
    protected Cache<String, Properties> resourcePropertiesCache = CacheBuilder.newBuilder().build();

    protected final static Properties PROPERTIES_NOT_FOUND = new Properties();

    @Autowired
    protected UserSessionSource userSessionSource;

    protected Locale getUserLocale() {
        return userSessionSource.checkCurrentUserSession() ?
                userSessionSource.getLocale() :
                getDefaultLocale();
    }

    protected Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    public String getMessage(Class caller, String key) {
        Locale loc = getUserLocale();
        return getMessage(caller, key, loc);
    }

    public String getMessage(Class caller, String key, Locale locale) {
        return getMessage(getPackName(caller), key, locale);
    }

    public String getMessage(Enum caller) {
        checkNotNullArgument(caller, "Enum parameter 'caller' is null");

        Locale loc = getUserLocale();
        return getMessage(caller, loc);
    }

    public String getMessage(Enum caller, Locale locale) {
        checkNotNullArgument(caller, "Enum parameter 'caller' is null");

        String declaringClassName = caller.getDeclaringClass().getName();
        int i = declaringClassName.lastIndexOf('.');
        if (i > -1)
            declaringClassName = declaringClassName.substring(i + 1);
        // If enum has inner subclasses, its class name ends with "$1", "$2", ... suffixes. Cut them off.

        return getMessage(
                getPackName(caller.getClass()),
                declaringClassName + "." + caller.name(),
                locale
        );
    }

    public String getMessage(String pack, String key) {
        Locale loc = getUserLocale();
        return getMessage(pack, key, loc);
    }

    public String getMessage(String packs, String key, Locale locale) {
        checkNotNullArgument(packs, "Messages pack name is null");
        checkNotNullArgument(key, "Message key is null");

        return internalGetMessage(packs, key, locale, key);
    }

    @Nullable
    public String findMessage(String packs, String key, @Nullable Locale locale) {
        checkNotNullArgument(packs, "Messages pack name is null");
        checkNotNullArgument(key, "Message key is null");

        if (locale == null)
            locale = getUserLocale();

        return internalGetMessage(packs, key, locale, null);
    }

    public void clearCache() {
        filePropertiesCache.invalidateAll();
        resourcePropertiesCache.invalidateAll();
        strCache.clear();
        notFoundCache.clear();
    }

    protected String internalGetMessage(String packs, String key, Locale locale, String defaultValue) {
//        locale = messageTools.trimLocale(locale);

        String cacheKey = makeCacheKey(packs, key, locale, locale);

        String msg = strCache.get(cacheKey);
        if (msg != null)
            return msg;

        String notFound = notFoundCache.get(cacheKey);
        if (notFound != null)
            return defaultValue;

        msg = searchMessage(packs, key, locale, locale, new HashSet<>());
        if (msg != null) {
            cache(cacheKey, msg);
            return msg;
        }

        notFoundCache.put(cacheKey, key);
        return defaultValue;
    }

    @Nullable
    protected String searchMessage(String packs, String key, Locale locale, Locale truncatedLocale, Set<String> passedPacks) {
        StringTokenizer tokenizer = new StringTokenizer(packs);
        //noinspection unchecked
        List<String> list = tokenizer.getTokenList();
        Collections.reverse(list);
        for (String pack : list) {
            if (!enterPack(pack, locale, truncatedLocale, passedPacks))
                continue;

            String msg = searchOnePack(pack, key, locale, truncatedLocale, passedPacks);
            if (msg != null)
                return msg;

            Locale tmpLocale = truncatedLocale;
            while (tmpLocale != null) {
                tmpLocale = truncateLocale(tmpLocale);
                msg = searchOnePack(pack, key, locale, tmpLocale, passedPacks);
                if (msg != null)
                    return msg;
            }
        }
        if (log.isTraceEnabled()) {
            String packName = new TextStringBuilder().appendWithSeparators(list, ",").toString();
            log.trace("Resource '{}' not found", makeCacheKey(packName, key, locale, locale));
        }
        return null;
    }

    private Locale truncateLocale(Locale locale) {
        if (locale == null
                || StringUtils.isEmpty(locale.getCountry()) && StringUtils.isEmpty(locale.getScript()))
            return null;
        return Locale.forLanguageTag(locale.getLanguage());
    }

    protected boolean enterPack(String pack, Locale locale, Locale truncatedLocale, Set<String> passedPacks) {
        String k = truncatedLocale == null ?
                pack + "/default" :
                pack + "/" + (locale == null ? "default" : LocaleResolver.localeToString(locale));
        return passedPacks.add(k);
    }

    protected String searchOnePack(String pack, String key, Locale locale, Locale truncatedLocale, Set<String> passedPacks) {
        String cacheKey = makeCacheKey(pack, key, locale, truncatedLocale);

        String msg = strCache.get(cacheKey);
        if (msg != null)
            return msg;

        msg = searchFiles(pack, key, locale, truncatedLocale, passedPacks);
        if (msg == null) {
            msg = searchClasspath(pack, key, locale, truncatedLocale, passedPacks);
        }
        return msg;
    }

    protected void cache(String key, String msg) {
        if (!strCache.containsKey(key))
            strCache.put(key, msg);
    }

    protected String searchFiles(String pack, String key, Locale locale, Locale truncatedLocale, Set<String> passedPacks) {
        String cacheKey = makeCacheKey(pack, key, locale, truncatedLocale);

        String msg = strCache.get(cacheKey);
        if (msg != null)
            return msg;

        log.trace("searchFiles: {}", cacheKey);

        String packPath = confDir + "/" + pack.replaceAll("\\.", "/");
        while (packPath != null && !packPath.equals(confDir)) {
            Properties properties = loadPropertiesFromFile(packPath, locale, truncatedLocale);
            if (properties != PROPERTIES_NOT_FOUND) {
                msg = getMessageFromProperties(pack, key, locale, truncatedLocale, properties, passedPacks);
                if (msg != null)
                    return msg;
            }
            // not found, keep searching
            int pos = packPath.lastIndexOf("/");
            if (pos < 0)
                packPath = null;
            else
                packPath = packPath.substring(0, pos);
        }
        return null;
    }

    protected String searchClasspath(String pack, String key, Locale locale, Locale truncatedLocale, Set<String> passedPacks) {
        String cacheKey = makeCacheKey(pack, key, locale, truncatedLocale);

        String msg = strCache.get(cacheKey);
        if (msg != null)
            return msg;

        log.trace("searchClasspath: {}", cacheKey);

        String packPath = "/" + pack.replaceAll("\\.", "/");
        while (packPath != null) {
            Properties properties = loadPropertiesFromResource(packPath, locale, truncatedLocale);
            if (properties != PROPERTIES_NOT_FOUND) {
                msg = getMessageFromProperties(pack, key, locale, truncatedLocale, properties, passedPacks);
                if (msg != null)
                    return msg;
            }
            // not found, keep searching
            int pos = packPath.lastIndexOf("/");
            if (pos < 0)
                packPath = null;
            else
                packPath = packPath.substring(0, pos);
        }
        return null;
    }

    @Nullable
    protected String getMessageFromProperties(String pack, String key, Locale locale, Locale truncatedLocale,
                                              Properties properties, Set<String> passedPacks) {
        String message;
        message = properties.getProperty(key);
        if (message != null) {
            cache(makeCacheKey(pack, key, locale, truncatedLocale), message);
            if (truncatedLocale == null)
                cache(makeCacheKey(pack, key, locale, null), message);
        }

        if (message == null) {
            // process includes after to support overriding
            message = searchIncludes(properties, key, locale, truncatedLocale, passedPacks);
        }
        return message;
    }

    @Nullable
    protected String searchIncludes(Properties properties, String key, Locale locale, Locale truncatedLocale,
                                    Set<String> passedPacks) {
        String includesProperty = properties.getProperty("@include");
        if (includesProperty != null) {
            // multiple includes separated by comma
            String[] includes = StringUtils.split(includesProperty, " ,");
            if (includes != null && includes.length > 0) {
                ArrayUtils.reverse(includes);
                for (String includePath : includes) {
                    includePath = StringUtils.trimToNull(includePath);
                    if (includePath != null) {
                        String message = searchMessage(includePath, key, locale, truncatedLocale, passedPacks);
                        if (message != null) {
                            return message;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Properties loadPropertiesFromFile(String packPath, Locale locale, Locale truncatedLocale) {
        String fileName = packPath + "/" + BUNDLE_NAME + getLocaleSuffix(truncatedLocale) + EXT;
        try {
            return filePropertiesCache.get(fileName, () -> {
                File file = new File(fileName);
                if (file.exists()) {
                    try (FileInputStream stream = new FileInputStream(file);
                         InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        Properties properties = new Properties();
                        properties.load(reader);
                        return properties;
                    }
                }
                return PROPERTIES_NOT_FOUND;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to load properties from file", e);
        }
    }

    protected Properties loadPropertiesFromResource(String packPath, Locale locale, Locale truncatedLocale) {
        String name = packPath + "/" + BUNDLE_NAME + getLocaleSuffix(truncatedLocale) + EXT;
        try {
            return resourcePropertiesCache.get(name, () -> {
                InputStream stream = getClass().getResourceAsStream(name);
                if (stream != null) {
                    try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        Properties properties = new Properties();
                        properties.load(reader);
                        return properties;
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }
                return PROPERTIES_NOT_FOUND;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to load properties from resource", e);
        }
    }

    protected String getLocaleSuffix(Locale locale) {
        return (locale != null ? "_" + LocaleResolver.localeToString(locale) : "");
    }

    protected String makeCacheKey(String pack, String key, @Nullable Locale locale, @Nullable Locale truncatedLocale) {
        if (truncatedLocale == null)
            return pack + "/default/" + key;

        return pack + "/" + (locale == null ? "default" : LocaleResolver.localeToString(locale)) + "/" + key;
    }

    protected String getPackName(Class c) {
        String className = c.getName();
        int pos = className.lastIndexOf(".");
        if (pos > 0)
            return className.substring(0, pos);
        else
            return "";
    }
}
