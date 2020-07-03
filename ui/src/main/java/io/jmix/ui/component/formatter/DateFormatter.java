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

import io.jmix.core.BeanLocator;
import io.jmix.core.LocaleResolver;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatypes;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.security.CurrentAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link Date} formatter to be used in screen descriptors.
 * <p>
 * Either {@code format} or {@code type} attributes should be defined in the {@code formatter} element.
 * <ul>
 *     <li> {@code format} - format string for {@code SimpleDateFormat};</li>
 *     <li> {@code type} - {@code DATE} or {@code DATETIME} - if specified, the value will be formatted
 *     by means of {@code DateDatatype} or {@code DateTimeDatatype} respectively;</li>
 *     <li> {@code useUserTimezone} - {@code true} to show the current user’s timezone.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 *      &lt;formatter name=&quot;ui_DateFormatter&quot; format=&quot;yyyy-MM-dd HH:mm:ss&quot;/&gt;
 * </pre>
 * Use {@link BeanLocator} when creating the formatter programmatically.
 */
@Component(DateFormatter.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateFormatter implements Formatter<Date> {

    public static final String NAME = "ui_DateFormatter";

    private Element element;

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Messages messages;

    public DateFormatter(Element element) {
        this.element = element;
    }

    @Override
    public String apply(Date value) {
        if (value == null) {
            return null;
        }
        String format = element.attributeValue("format");
        if (StringUtils.isBlank(format)) {
            String type = element.attributeValue("type");
            if (type != null) {
                FormatStrings formatStrings = Datatypes.getFormatStrings(currentAuthentication.getLocale());
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
            if (format.startsWith("msg://")) {
                format = messages.getMessage(format.substring(6));
            }
            DateFormat df = new SimpleDateFormat(format);

            if (Boolean.parseBoolean(element.attributeValue("useUserTimezone")) &&
                    currentAuthentication.isSet()) {
                df.setTimeZone(currentAuthentication.getTimeZone());
            }

            return df.format(value);
        }
    }
}
