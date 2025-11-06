/*
 * Copyright 2025 Haulmont.
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

package io.jmix.security.util;

import io.jmix.core.CoreProperties;
import io.jmix.core.security.DeviceTimeZoneProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

//todo Javadoc
@Component("sec_ClientDetailsSourceSupport")
public class ClientDetailsSourceSupport {

    protected final RequestLocaleProvider requestLocaleProvider;
    protected final DeviceTimeZoneProvider deviceTimeZoneProvider;
    protected final CoreProperties coreProperties;

    public ClientDetailsSourceSupport(RequestLocaleProvider requestLocaleProvider,
                                      DeviceTimeZoneProvider deviceTimeZoneProvider,
                                      CoreProperties coreProperties) {
        this.requestLocaleProvider = requestLocaleProvider;
        this.deviceTimeZoneProvider = deviceTimeZoneProvider;
        this.coreProperties = coreProperties;
    }

    public Locale getLocale(HttpServletRequest request) {
        Locale locale = requestLocaleProvider.getLocale(request);
        return locale == null ? getDefaultLocale() : locale;
    }

    public Locale getDefaultLocale() {
        List<Locale> locales = coreProperties.getAvailableLocales();
        return locales.get(0);
    }

    public TimeZone getTimeZone() {
        TimeZone deviceTimeZone = getDeviceTimeZone();
        return deviceTimeZone == null ? TimeZone.getDefault() : deviceTimeZone;
    }

    @Nullable
    public TimeZone getDeviceTimeZone() {
        return deviceTimeZoneProvider.getDeviceTimeZone();
    }
}