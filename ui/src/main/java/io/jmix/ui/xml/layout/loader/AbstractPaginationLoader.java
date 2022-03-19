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

package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Splitter;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.PaginationComponent;
import io.jmix.ui.component.pagination.data.PaginationContainerBinder;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import io.jmix.ui.component.pagination.data.PaginationLoaderBinder;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPaginationLoader<T extends PaginationComponent> extends AbstractComponentLoader<T> {

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);
        loadStyleName(resultComponent, element);
        loadVisible(resultComponent, element);
        loadAlign(resultComponent, element);
        loadCss(resultComponent, element);

        loadBoolean(element, "itemsPerPageVisible",
                resultComponent::setItemsPerPageVisible);
        loadBoolean(element, "itemsPerPageUnlimitedOptionVisible",
                resultComponent::setItemsPerPageUnlimitedOptionVisible);

        loadString(element, "itemsPerPageOptions")
                .ifPresent(options -> {
                    String resourceOptions = loadResourceString(options);
                    if (StringUtils.isNotEmpty(resourceOptions)) {
                        resultComponent.setItemsPerPageOptions(parseItemsPerPageOptions(resourceOptions));
                    }
                });

        loadInteger(element, "itemsPerPageDefaultValue",
                resultComponent::setItemsPerPageDefaultValue);

        loadDataSourceProvider(element);
    }

    protected void loadDataSourceProvider(Element element) {
        Element loaderProvider = element.element("loaderProvider");
        Element containerProvider = element.element("containerProvider");

        if (loaderProvider != null && containerProvider != null) {
            throw new GuiDevelopmentException("Pagination must have only one provider: 'loaderProvider' or 'containerProvider'",
                    getContext());
        }

        if (loaderProvider != null) {
            loadString(loaderProvider, "loaderId", id -> {
                ScreenData screenData = UiControllerUtils.getScreenData(getComponentContext().getFrame().getFrameOwner());
                DataLoader loader = screenData.getLoader(id);
                if (loader instanceof BaseCollectionLoader) {
                    PaginationDataBinder dataSourceProvider =
                            applicationContext.getBean(PaginationLoaderBinder.class, loader);
                    resultComponent.setDataBinder(dataSourceProvider);
                } else {
                    throw new GuiDevelopmentException(
                            String.format("PaginationDataSourceProvider does not support %s loader type",
                                    loader.getClass().getCanonicalName()), getContext());
                }
            });

            if (resultComponent.getDataBinder() == null) {
                throw new GuiDevelopmentException("Specify 'loaderId' attribute of `loaderProvider` element",
                        getContext());
            }
        }

        if (containerProvider != null) {
            loadString(containerProvider, "dataContainer", containerId -> {
                ScreenData screenData = UiControllerUtils.getScreenData(getComponentContext().getFrame().getFrameOwner());
                InstanceContainer container = screenData.getContainer(containerId);
                if (container instanceof CollectionContainer) {
                    PaginationDataBinder dataSourceProvider = applicationContext
                            .getBean(PaginationContainerBinder.class, container);
                    resultComponent.setDataBinder(dataSourceProvider);
                } else {
                    throw new GuiDevelopmentException(
                            String.format("PaginationDataSourceProvider does not support %s container type",
                                    container.getClass().getCanonicalName()), getContext());
                }
            });

            if (resultComponent.getDataBinder() == null) {
                throw new GuiDevelopmentException("Specify 'dataContainer' attribute of `containerProvider` element",
                        getContext());
            }
        }
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
