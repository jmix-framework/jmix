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
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.SuggestionFieldComponent;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.substitutor.StringSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class AbstractSuggestionFieldLoader<T extends SuggestionFieldComponent> extends AbstractFieldLoader<T> {

    protected void loadQuery(T suggestionField, Element element) {
        Element queryElement = element.element("query");
        if (queryElement != null) {
            final boolean escapeValue;

            String stringQuery = queryElement.getStringValue();

            String searchFormat = queryElement.attributeValue("searchStringFormat");

            String fetchPlan = loadFetchPlan(queryElement);

            String escapeValueForLike = queryElement.attributeValue("escapeValueForLike");
            if (StringUtils.isNotEmpty(escapeValueForLike)) {
                escapeValue = Boolean.parseBoolean(escapeValueForLike);
            } else {
                escapeValue = false;
            }

            String entityClassName = queryElement.attributeValue("entityClass");
            if (StringUtils.isNotEmpty(entityClassName)) {
                DataManager dataManager = applicationContext.getBean(DataManager.class);
                suggestionField.setSearchExecutor((searchString, searchParams) -> {
                    Class<?> entityClass = ReflectionHelper.getClass(entityClassName);
                    if (escapeValue) {
                        searchString = QueryUtils.escapeForLike(searchString);
                    }
                    searchString = applySearchFormat(searchString, searchFormat);

                    FluentLoader.ByQuery<?> loader = dataManager.load(entityClass)
                            .query(stringQuery)
                            .parameter("searchString", searchString);
                    if (!Strings.isNullOrEmpty(fetchPlan)) {
                        FetchPlanRepository fetchPlanRepository = applicationContext.getBean(FetchPlanRepository.class);
                        loader.fetchPlan(fetchPlanRepository.getFetchPlan(entityClass, fetchPlan));
                    }
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
            StringSubstitutor substitutor = applicationContext.getBean(StringSubstitutor.class);
            searchString = substitutor.substitute(format, ParamsMap.of("searchString", searchString));
        }
        return searchString;
    }

    protected String loadFetchPlan(Element queryElement) {
        return queryElement.attributeValue("fetchPlan");
    }

    protected void loadPopupWidth(T suggestionField, Element element) {
        String popupWidth = element.attributeValue("popupWidth");
        if (StringUtils.isNotEmpty(popupWidth)) {
            suggestionField.setPopupWidth(popupWidth);
        }
    }

    protected void loadAsyncSearchDelayMs(T suggestionField, Element element) {
        String asyncSearchDelayMs = element.attributeValue("asyncSearchDelayMs");
        if (StringUtils.isNotEmpty(asyncSearchDelayMs)) {
            suggestionField.setAsyncSearchDelayMs(Integer.parseInt(asyncSearchDelayMs));
        }
    }

    protected void loadMinSearchStringLength(T suggestionField, Element element) {
        String minSearchStringLength = element.attributeValue("minSearchStringLength");
        if (StringUtils.isNotEmpty(minSearchStringLength)) {
            suggestionField.setMinSearchStringLength(Integer.parseInt(minSearchStringLength));
        }
    }

    protected void loadSuggestionsLimit(T suggestionField, Element element) {
        String suggestionsLimit = element.attributeValue("suggestionsLimit");
        if (StringUtils.isNotEmpty(suggestionsLimit)) {
            suggestionField.setSuggestionsLimit(Integer.parseInt(suggestionsLimit));
        }
    }

    protected void loadCaptionProperty(T suggestionField, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (StringUtils.isNotEmpty(captionProperty)) {
            suggestionField.setFormatter(
                    new CaptionAdapter(captionProperty,
                            applicationContext.getBean(Metadata.class),
                            applicationContext.getBean(MetadataTools.class))
            );
        }
    }
}
