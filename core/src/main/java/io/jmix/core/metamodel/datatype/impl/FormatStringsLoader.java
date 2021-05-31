/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("core_FormatStringsLoader")
public class FormatStringsLoader {

    private static final Logger log = LoggerFactory.getLogger(FormatStringsLoader.class);

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @EventListener(ContextRefreshedEvent.class)
    private void init() {
        for (Locale locale : coreProperties.getAvailableLocales()) {
            String numberDecimalSeparator = getMessage("numberDecimalSeparator", locale);
            String numberGroupingSeparator = getMessage("numberGroupingSeparator", locale);
            String integerFormat = getMessage("integerFormat", locale);
            String doubleFormat = getMessage("doubleFormat", locale);
            String decimalFormat = getMessage("decimalFormat", locale);
            String dateFormat = getMessage("dateFormat", locale);
            String dateTimeFormat = getMessage("dateTimeFormat", locale);
            String offsetDateTimeFormat = getMessage("offsetDateTimeFormat", locale);
            String timeFormat = getMessage("timeFormat", locale);
            String offsetTimeFormat = getMessage("offsetTimeFormat", locale);
            String trueString = getMessage("trueString", locale);
            String falseString = getMessage("falseString", locale);
            if (numberDecimalSeparator.equals("numberDecimalSeparator")
                    || numberGroupingSeparator.equals("numberGroupingSeparator")
                    || integerFormat.equals("integerFormat")
                    || doubleFormat.equals("doubleFormat")
                    || decimalFormat.equals("decimalFormat")
                    || dateFormat.equals("dateFormat")
                    || dateTimeFormat.equals("dateTimeFormat")
                    || offsetDateTimeFormat.equals("offsetDateTimeFormat")
                    || timeFormat.equals("timeFormat")
                    || offsetTimeFormat.equals("offsetTimeFormat"))
                log.warn("Localized format strings are not defined for {}", locale);

            formatStringsRegistry.setFormatStrings(
                    locale.stripExtensions(),
                    new FormatStrings(
                            numberDecimalSeparator.charAt(0),
                            numberGroupingSeparator.charAt(0),
                            integerFormat,
                            doubleFormat,
                            decimalFormat,
                            dateFormat,
                            dateTimeFormat,
                            offsetDateTimeFormat,
                            timeFormat,
                            offsetTimeFormat,
                            trueString,
                            falseString
                    )
            );
        }
    }

    protected String getMessage(String key, Locale locale) {
        return messages.getMessage(key, locale);
    }
}
