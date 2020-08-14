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

package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.SuggestionField;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class SuggestionFieldQueryLoader<T extends Field> extends AbstractFieldLoader<T> {

    protected void loadQuery(SuggestionField suggestionField, Element element) {
        Element queryElement = element.element("query");
        if (queryElement != null) {
            final boolean escapeValue;

            String stringQuery = queryElement.getStringValue();

            String searchFormat = queryElement.attributeValue("searchStringFormat");

            String view = queryElement.attributeValue("view");

            String escapeValueForLike = queryElement.attributeValue("escapeValueForLike");
            if (StringUtils.isNotEmpty(escapeValueForLike)) {
                escapeValue = Boolean.parseBoolean(escapeValueForLike);
            } else {
                escapeValue = false;
            }

            String entityClassName = queryElement.attributeValue("entityClass");
            if (StringUtils.isNotEmpty(entityClassName)) {
                DataManager dataManager = (DataManager) applicationContext.getBean(DataManager.NAME);
                suggestionField.setSearchExecutor((searchString, searchParams) -> {
                    Class<JmixEntity> entityClass = ReflectionHelper.getClass(entityClassName);
                    if (escapeValue) {
                        searchString = QueryUtils.escapeForLike(searchString);
                    }
                    searchString = applySearchFormat(searchString, searchFormat);

                    FluentLoader<JmixEntity> loader = dataManager.load(entityClass);
                    if (!Strings.isNullOrEmpty(view)) {
                        loader.fetchPlan(applicationContext.getBean(FetchPlanRepository.class).getFetchPlan(entityClass, view));
                    }
                    loader.query(stringQuery)
                            .parameter("searchString", searchString);
                    return loader.list();
                });
            } else {
                throw new GuiDevelopmentException(String.format("Field 'entityClass' is empty in component %s.",
                        suggestionField.getId()), getContext());
            }
        }
    }

    protected String applySearchFormat(String searchString, String format) {
        if (StringUtils.isNotEmpty(format)) {
            // todo GStringTemplateEngine
//            GStringTemplateEngine engine = new GStringTemplateEngine();
//            StringWriter writer = new StringWriter();
//            try {
//                engine.createTemplate(format).make(ParamsMap.of("searchString", searchString)).writeTo(writer);
//                return writer.toString();
//            } catch (ClassNotFoundException | IOException e) {
//                throw new IllegalStateException(e);
//            }
        }
        return searchString;
    }
}
