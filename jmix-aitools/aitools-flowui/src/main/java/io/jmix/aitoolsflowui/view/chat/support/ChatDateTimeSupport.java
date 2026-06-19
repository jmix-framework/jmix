/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitoolsflowui.view.chat.support;

import io.jmix.core.DateTimeTransformations;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.security.CurrentAuthentication;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Renders chat timestamps in the current user's time zone.
 */
@Component("aitls_ChatDateTimeSupport")
public class ChatDateTimeSupport {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected DateTimeTransformations dateTimeTransformations;
    @Autowired
    protected DatatypeFormatter datatypeFormatter;

    /**
     * Returns the current user's time zone (the application default when the user has none).
     *
     * @return the current user's zone id
     */
    public ZoneId getCurrentUserZone() {
        return currentAuthentication.getTimeZone().toZoneId();
    }

    /**
     * Formats the timestamp in the current user's time zone, keeping the same instant.
     *
     * @param value timestamp to format, may be {@code null}
     * @return the formatted timestamp, or an empty string when {@code value} is {@code null}
     */
    public String formatInUserZone(@Nullable OffsetDateTime value) {
        if (value == null) {
            return "";
        }
        OffsetDateTime inUserZone = (OffsetDateTime) dateTimeTransformations.transformToType(
                value, OffsetDateTime.class, getCurrentUserZone());
        return datatypeFormatter.formatOffsetDateTime(inUserZone);
    }
}
