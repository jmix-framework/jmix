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
package io.jmix.ui.component.formatter;


import io.jmix.core.DateTimeTransformations;
import io.jmix.core.LocaleResolver;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * {@link Date} formatter to be used in screen descriptors and controllers.
 * <p>
 * This formatter formats the {@link Date} value into a string depending on the format string.
 */
@StudioElement(
        caption = "DateFormatter",
        xmlElement = "date",
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox", "io.jmix.ui.component.mainwindow.UserIndicator"},
        icon = "io/jmix/ui/icon/element/formatter.svg"
)
@Component("ui_DateFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateFormatter<V> implements Formatter<V> {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Messages messages;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;
    @Autowired
    protected DateTimeTransformations dateTimeTransformations;

    protected String format;
    protected String type;
    protected boolean useUserTimezone;

    /**
     * Sets the format string describing the date format which will be used to create {@link SimpleDateFormat} instance.
     * It can be either a format string, or a key in message group.
     *
     * @param format a format string or a key in message group
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Sets the formatter type, which can have a {@code DATE} or {@code DATETIME} value. If specified,
     * the value will be formatted by means of {@code DateDatatype} or {@code DateTimeDatatype} respectively.
     *
     * @param type a formatter type
     */
    @StudioProperty(type = PropertyType.ENUMERATION, options = {"DATE", "DATETIME"})
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets whether formatter should display the date and time in the current user's timezone.
     *
     * @param useUserTimezone {@code true} if the formatter displays the date and time in the current user's timezone,
     *                        {@code false} otherwise
     */
    @StudioProperty(defaultValue = "false")
    public void setUseUserTimezone(boolean useUserTimezone) {
        this.useUserTimezone = useUserTimezone;
    }

    @Nullable
    @Override
    public String apply(@Nullable V value) {
        if (value == null) {
            return null;
        }

        if (StringUtils.isBlank(format)) {
            if (type != null) {
                FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
                if (formatStrings == null)
                    throw new IllegalStateException("FormatStrings are not defined for " +
                            LocaleResolver.localeToString(currentAuthentication.getLocale()));
                switch (type) {
                    case "DATE":
                        format = formatStrings.getDateFormat();
                        break;
                    case "DATETIME":
                        format = formatStrings.getDateTimeFormat();
                        break;
                    default:
                        throw new RuntimeException("Illegal formatter type value");
                }
            }
        }

        if (StringUtils.isBlank(format)) {
            return value.toString();
        } else {
            return applyFormatInternal(value);
        }
    }

    protected String applyFormatInternal(V value) {
        if (format.startsWith("msg://")) {
            format = messages.getMessage(format.substring(6));
        }

        ZonedDateTime zonedDateTime = dateTimeTransformations.transformToZDT(value);
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);

        if (currentAuthentication.isSet()) {
            df = df.withLocale(currentAuthentication.getLocale());

            if (useUserTimezone
                    && dateTimeTransformations.isDateTypeSupportsTimeZones(value.getClass())) {
                ZoneId zoneId = currentAuthentication.getTimeZone().toZoneId();

                zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
                df = df.withZone(zoneId);
            }
        }

        return df.format(zonedDateTime);
    }
}
