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


import io.jmix.core.LocaleResolver;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

/**
 * Number formatter to be used in screen descriptors and controllers.
 * <p>
 * This formatter formats the {@link Number} value into a string depending on the format string.
*/
@StudioElement(
        caption = "NumberFormatter",
        xmlElement = "number",
        unsupportedTarget = {"io.jmix.ui.component.EntityComboBox", "io.jmix.ui.component.mainwindow.UserIndicator"},
        icon = "io/jmix/ui/icon/element/formatter.svg"
)
@Component("ui_NumberFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NumberFormatter implements Formatter<Number> {

    protected String format;

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Messages messages;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    /**
     * Sets the format string describing the number format which will be used to create {@link DecimalFormat} instance.
     * It can be either a format string, or a key in message group.
     *
     * @param format a format string or a key in message group
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    public void setFormat(String format) {
        this.format = format;
    }

    @Nullable
    @Override
    public String apply(@Nullable Number value) {
        if (value == null) {
            return null;
        }

        if (format == null) {
            Datatype datatype = datatypeRegistry.get(value.getClass());
            return datatype.format(value, currentAuthentication.getLocale());
        } else {
            if (format.startsWith("msg://")) {
                format = messages.getMessage(format.substring(6));
            }
            FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
            if (formatStrings == null)
                throw new IllegalStateException("FormatStrings are not defined for " +
                        LocaleResolver.localeToString(currentAuthentication.getLocale()));
            DecimalFormat decimalFormat = new DecimalFormat(format, formatStrings.getFormatSymbols());
            return decimalFormat.format(value);
        }
    }
}
