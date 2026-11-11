/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.component.data;

import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.annotation.IgnoreUserTimeZone;
import io.jmix.core.metamodel.datatype.TimeZoneAwareDatatype;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.RandomStringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * INTERNAL.
 * <p>
 * Utility class for data providers.
 */
@Internal
public final class DataProviderUtils {

    private DataProviderUtils() {
    }

    /**
     * Generates an ID for data providers
     *
     * @return a string ID
     */
    public static String generateId() {
        return RandomStringUtils.randomAlphabetic(6);
    }

    /**
     * Returns whether the start date-time property of the data provider's entity holds a moment in time that is
     * shown in the user's time zone, such as {@code OffsetDateTime} or {@code Date} with
     * {@code TemporalType.TIMESTAMP} without {@link IgnoreUserTimeZone}.
     *
     * @param dataProvider data provider, {@code null} gives {@code false}
     * @return {@code true} if the data provider's events have a time zone aware start date-time
     */
    public static boolean isStartDateTimeZoneAware(@Nullable CalendarDataProvider dataProvider) {
        return dataProvider instanceof EntityCalendarDataProvider entityDataProvider
                && isTimeZoneAware(entityDataProvider, entityDataProvider.getStartDateTimeProperty());
    }

    /**
     * Returns whether the end date-time property of the data provider's entity holds a moment in time that is
     * shown in the user's time zone, such as {@code OffsetDateTime} or {@code Date} with
     * {@code TemporalType.TIMESTAMP} without {@link IgnoreUserTimeZone}.
     *
     * @param dataProvider data provider, {@code null} gives {@code false}
     * @return {@code true} if the data provider's events have a time zone aware end date-time
     */
    public static boolean isEndDateTimeZoneAware(@Nullable CalendarDataProvider dataProvider) {
        return dataProvider instanceof EntityCalendarDataProvider entityDataProvider
                && isTimeZoneAware(entityDataProvider, entityDataProvider.getEndDateTimeProperty());
    }

    private static boolean isTimeZoneAware(EntityCalendarDataProvider dataProvider, @Nullable String property) {
        if (Strings.isNullOrEmpty(property)) {
            return false;
        }

        // A retriever that loads events only through its load delegate may have no entity class
        if (dataProvider instanceof EntityCalendarDataRetriever retriever && retriever.getEntityClass() == null) {
            return false;
        }

        MetaProperty metaProperty = dataProvider.getEntityMetaClass().findProperty(property);
        if (metaProperty == null
                || !metaProperty.getRange().isDatatype()
                || !(metaProperty.getRange().asDatatype() instanceof TimeZoneAwareDatatype)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> ignoreUserTimeZone =
                (Map<String, Object>) metaProperty.getAnnotations().get(IgnoreUserTimeZone.class.getName());

        return ignoreUserTimeZone == null || !Boolean.TRUE.equals(ignoreUserTimeZone.get("value"));
    }
}
