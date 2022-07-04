/*
 * Copyright 2022 Haulmont.
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
package io.jmix.flowui.component.formatter;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.kit.component.formatter.Formatter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

/**
 * Number formatter to be used in view descriptors and controllers.
 * <p>
 * This formatter formats the {@link Number} value into a string depending on the format string.
 */
@Component("flowui_NumberFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NumberFormatter implements Formatter<Number> {

    protected String format;

    protected CurrentAuthentication currentAuthentication;
    protected DatatypeRegistry datatypeRegistry;
    protected FormatStringsRegistry formatStringsRegistry;

    public NumberFormatter(CurrentAuthentication currentAuthentication, DatatypeRegistry datatypeRegistry,
                           FormatStringsRegistry formatStringsRegistry) {
        this.currentAuthentication = currentAuthentication;
        this.datatypeRegistry = datatypeRegistry;
        this.formatStringsRegistry = formatStringsRegistry;
    }

    /**
     * Sets the format string describing the number format which will be used to create {@link DecimalFormat} instance.
     * It can be either a format string, or a key in message group.
     *
     * @param format a format string or a key in message group
     */
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
            Datatype<?> datatype = datatypeRegistry.get(value.getClass());
            return datatype.format(value, currentAuthentication.getLocale());
        } else {
            FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
            DecimalFormat decimalFormat = new DecimalFormat(format, formatStrings.getFormatSymbols());
            return decimalFormat.format(value);
        }
    }
}
