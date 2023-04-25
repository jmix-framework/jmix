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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Splitter;
import io.jmix.flowui.data.pagination.PaginationDataLoaderImpl;
import io.jmix.flowui.data.pagination.PaginationDataLoader;
import io.jmix.flowui.component.pagination.SimplePagination;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class SimplePaginationLoader extends AbstractComponentLoader<SimplePagination> {

    @Override
    protected SimplePagination createComponent() {
        return factory.create(SimplePagination.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);

        loadBoolean(element, "itemsPerPageUnlimitedItemVisible",
                resultComponent::setItemsPerPageUnlimitedItemVisible);
        loadInteger(element, "itemsPerPageDefaultValue", resultComponent::setItemsPerPageDefaultValue);

        loadResourceString(element, "itemsPerPageItems", context.getMessageGroup())
                .ifPresent(items -> resultComponent.setItemsPerPageItems(parseItemsPerPageOptions(items)));

        loadBoolean(element, "itemsPerPageVisible", resultComponent::setItemsPerPageVisible);

        loadLoader(element);
    }

    protected void loadLoader(Element element) {
        loadString(element, "dataLoader")
                .ifPresent(id -> {
                    ViewData screenData = ViewControllerUtils.getViewData(getComponentContext().getView());
                    DataLoader loader = screenData.getLoader(id);
                    if (loader instanceof BaseCollectionLoader) {
                        PaginationDataLoader paginationLoader =
                                applicationContext.getBean(PaginationDataLoaderImpl.class, loader);
                        resultComponent.setPaginationLoader(paginationLoader);
                    } else {
                        throw new GuiDevelopmentException(
                                String.format(PaginationDataLoader.class.getSimpleName() +
                                                " does not support %s loader type",
                                        loader.getClass().getCanonicalName()), getContext());
                    }
                });
    }

    public List<Integer> parseItemsPerPageOptions(String maxResults) {
        Iterable<String> split = Splitter.on(",").trimResults().split(maxResults);

        List<Integer> result = new ArrayList<>();
        for (String option : split) {
            try {
                result.add(Integer.parseInt(option));
            } catch (NumberFormatException e) {
                throw new GuiDevelopmentException("Cannot parse to Integer: " + option, getContext());
            }
        }

        return result;
    }
}
