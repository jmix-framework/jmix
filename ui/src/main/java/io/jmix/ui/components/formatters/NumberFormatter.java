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
package io.jmix.ui.components.formatters;

import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.Datatypes;
import io.jmix.core.metamodel.datatypes.FormatStrings;
import io.jmix.core.security.UserSessionSource;
import org.dom4j.Element;

import java.text.DecimalFormat;
import java.util.function.Function;

/**
 * Number formatter to be used in screen descriptors and controllers.
 * <br> If defined in XML together with {@code format} attribute, uses this format, otherwise formats by means of
 * {@link Datatype#format(Object, java.util.Locale)}.
 */
public class NumberFormatter implements Function<Number, String> {

    private Element element;

    protected UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);
    protected Messages messages = AppBeans.get(Messages.NAME);

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
            return datatype.format(value, userSessionSource.getLocale());
        } else {
            if (pattern.startsWith("msg://")) {
                pattern = messages.getMessage(pattern.substring(6, pattern.length()));
            }
            FormatStrings formatStrings = Datatypes.getFormatStrings(userSessionSource.getLocale());
            if (formatStrings == null)
                throw new IllegalStateException("FormatStrings are not defined for " + userSessionSource.getLocale());
            DecimalFormat format = new DecimalFormat(pattern, formatStrings.getFormatSymbols());
            return format.format(value);
        }
    }
}
