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

import com.google.common.collect.ImmutableMap;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * Specific bean for loading formatters.
 */
@Component("flowui_FormatterLoadFactory")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FormatterLoadFactory {

    protected ApplicationContext applicationContext;
    protected LoaderSupport loaderSupport;
    protected Context context;

    protected final Map<String, Function<Element, ? extends Formatter<?>>> FORMATTERS_MAP =
            ImmutableMap.<String, Function<Element, ? extends Formatter<?>>>builder()
                    .put("collection", this::loadCollectionFormatter)
                    .put("custom", this::loadCustomFormatter)
                    .put("date", this::loadDateFormatter)
                    .put("number", this::loadNumberFormatter)
                    .build();

    public FormatterLoadFactory(Context context) {
        this.context = context;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    /**
     * Creates a formatter from XML element.
     *
     * @param element XML element
     * @return formatter or null if there is no such element
     */
    @Nullable
    public Formatter<?> createFormatter(Element element) {
        Function<Element, ? extends Formatter<?>> function = FORMATTERS_MAP.get(element.getName());
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

    protected Formatter<?> loadCustomFormatter(Element element) {
        String bean = loaderSupport.loadString(element, "bean")
                .orElse("");

        if (StringUtils.isEmpty(bean)) {
            throw new IllegalArgumentException("Bean name is not defined");
        }

        return (Formatter<?>) applicationContext.getBean(bean);
    }

    protected DateFormatter<?> loadDateFormatter(Element element) {
        DateFormatter<?> formatter = applicationContext.getBean(DateFormatter.class);

        loaderSupport.loadResourceString(element, "format", context.getMessageGroup(), formatter::setFormat);
        loaderSupport.loadString(element, "type", formatter::setType);
        loaderSupport.loadBoolean(element, "useUserTimeZone", formatter::setUseUserTimezone);

        return formatter;
    }

    protected NumberFormatter loadNumberFormatter(Element element) {
        NumberFormatter formatter = applicationContext.getBean(NumberFormatter.class);

        loaderSupport.loadResourceString(element, "format", context.getMessageGroup(), formatter::setFormat);

        return formatter;
    }
}
