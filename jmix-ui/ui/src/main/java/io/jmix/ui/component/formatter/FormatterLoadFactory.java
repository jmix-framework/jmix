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
import io.jmix.core.MessageTools;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Specific bean for loading formatters.
 */
@Component("ui_FormatterLoadFactory")
public class FormatterLoadFactory {

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;

    protected final Map<String, BiFunction<Element, Context, ? extends Formatter>> FORMATTERS_MAP =
            ImmutableMap.<String, BiFunction<Element, Context, ? extends Formatter>>builder()
                    .put("collection", this::loadCollectionFormatter)
                    .put("custom", this::loadCustomFormatter)
                    .put("date", this::loadDateFormatter)
                    .put("number", this::loadNumberFormatter)
                    .build();

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    /**
     * Creates a formatter from XML element.
     *
     * @param element XML element
     * @return formatter or null if there is no such element
     */
    @Nullable
    public Formatter<?> createFormatter(Element element, Context context) {
        BiFunction<Element, Context, ? extends Formatter> function = FORMATTERS_MAP.get(element.getName());
        if (function != null) {
            return function.apply(element, context);
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

    protected CollectionFormatter loadCollectionFormatter(Element element, Context context) {
        return applicationContext.getBean(CollectionFormatter.class);
    }

    protected Formatter loadCustomFormatter(Element element, Context context) {
        String bean = element.attributeValue("bean");
        if (StringUtils.isEmpty(bean)) {
            throw new IllegalArgumentException("Bean name is not defined");
        }

        return (Formatter) applicationContext.getBean(bean);
    }

    protected DateFormatter loadDateFormatter(Element element, Context context) {
        DateFormatter formatter = applicationContext.getBean(DateFormatter.class);

        String format = element.attributeValue("format");
        if (StringUtils.isNotEmpty(format)) {
            String loadedFormat = loadFormat(format, context.getMessageGroup());
            formatter.setFormat(loadedFormat);
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

    protected NumberFormatter loadNumberFormatter(Element element, Context context) {
        NumberFormatter formatter = applicationContext.getBean(NumberFormatter.class);

        String format = element.attributeValue("format");
        if (StringUtils.isNotEmpty(format)) {
            String loadedFormat = loadFormat(format, context.getMessageGroup());
            formatter.setFormat(loadedFormat);
        }

        return formatter;
    }

    protected String loadFormat(String format, @Nullable String messageGroup) {
        return messageTools.loadString(messageGroup, format);
    }

    public static class Context {

        protected String messageGroup;

        @Nullable
        public String getMessageGroup() {
            return messageGroup;
        }

        public void setMessageGroup(@Nullable String messageGroup) {
            this.messageGroup = messageGroup;
        }
    }
}
