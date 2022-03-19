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

package io.jmix.ui.component.formatter;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationContext;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * Specific bean for loading formatters.
 */
@Component("ui_FormatterLoadFactory")
public class FormatterLoadFactory {

    protected ApplicationContext applicationContext;

    protected final Map<String, Function<Element, ? extends Formatter>> FORMATTERS_MAP =
            ImmutableMap.<String, Function<Element, ? extends Formatter>>builder()
                    .put("collection", this::loadCollectionFormatter)
                    .put("custom", this::loadCustomFormatter)
                    .put("date", this::loadDateFormatter)
                    .put("number", this::loadNumberFormatter)
                    .build();

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates a formatter from XML element.
     *
     * @param element XML element
     * @return formatter or null if there is no such element
     */
    @Nullable
    public Formatter<?> createFormatter(Element element) {
        Function<Element, ? extends Formatter> function = FORMATTERS_MAP.get(element.getName());
        if (function != null) {
            return function.apply(element);
        }
        return null;
    }

    /**
     * Checks if XML element is a formatter.
     *
     * @param element XML element
     * @return {@code true} if the element is formatter, {@code false} otherwise
     */
    public boolean isFormatter(Element element) {
        return FORMATTERS_MAP.get(element.getName()) != null;
    }

    protected CollectionFormatter loadCollectionFormatter(Element element) {
        return applicationContext.getBean(CollectionFormatter.class);
    }

    protected Formatter loadCustomFormatter(Element element) {
        String bean = element.attributeValue("bean");
        if (StringUtils.isEmpty(bean)) {
            throw new IllegalArgumentException("Bean name is not defined");
        }

        return (Formatter) applicationContext.getBean(bean);
    }

    protected DateFormatter loadDateFormatter(Element element) {
        DateFormatter formatter = applicationContext.getBean(DateFormatter.class);

        String format = element.attributeValue("format");
        if (StringUtils.isNotEmpty(format)) {
            formatter.setFormat(format);
        }

        String type = element.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            formatter.setType(type);
        }

        String useUserTimezone = element.attributeValue("useUserTimezone");
        if (StringUtils.isNotEmpty(useUserTimezone)) {
            formatter.setUseUserTimezone(Boolean.parseBoolean(useUserTimezone));
        }

        return formatter;
    }

    protected NumberFormatter loadNumberFormatter(Element element) {
        NumberFormatter formatter = applicationContext.getBean(NumberFormatter.class);

        String format = element.attributeValue("format");
        if (StringUtils.isNotEmpty(format)) {
            formatter.setFormat(format);
        }

        return formatter;
    }
}
