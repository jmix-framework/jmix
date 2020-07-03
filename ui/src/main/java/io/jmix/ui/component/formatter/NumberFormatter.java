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
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.Datatypes;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.security.CurrentAuthentication;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

/**
 * Number formatter to be used in screen descriptors and controllers.
 * <p>
 * If defined in XML together with {@code format} attribute, uses this format, otherwise formats by means of
 * {@link Datatype#format(Object, java.util.Locale)}.
 * <p>
 * Example usage:
 * <pre>
 *      &lt;formatter name=&quot;ui_NumberFormatter&quot; format=&quot;%f&quot;/&gt;
 * </pre>
 * Use {@link BeanLocator} when creating the formatter programmatically.
 */
@Component(NumberFormatter.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NumberFormatter implements Formatter<Number> {

    public static final String NAME = "ui_NumberFormatter";

    private Element element;

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Messages messages;

    public NumberFormatter() {
    }

    public NumberFormatter(Element element) {
        this.element = element;
    }

    @Override
    public String apply(Number value) {
        if (value == null) {
            return null;
        }
        String pattern = element != null ? element.attributeValue("format") : null;

        if (pattern == null) {
            Datatype datatype = Datatypes.getNN(value.getClass());
            return datatype.format(value, currentAuthentication.getLocale());
        } else {
            if (pattern.startsWith("msg://")) {
                pattern = messages.getMessage(pattern.substring(6));
            }
            FormatStrings formatStrings = Datatypes.getFormatStrings(currentAuthentication.getLocale());
            if (formatStrings == null)
                throw new IllegalStateException("FormatStrings are not defined for " +
                        LocaleResolver.localeToString(currentAuthentication.getLocale()));
            DecimalFormat format = new DecimalFormat(pattern, formatStrings.getFormatSymbols());
            return format.format(value);
        }
    }
}
