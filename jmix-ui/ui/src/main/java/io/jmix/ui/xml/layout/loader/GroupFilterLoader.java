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

import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.GroupFilter;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.SupportsCaptionPosition;
import io.jmix.ui.component.impl.FilterLoaderUtils;
import io.jmix.ui.component.impl.GroupFilterImpl;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

public class GroupFilterLoader extends AbstractComponentLoader<GroupFilter> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(GroupFilter.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadHtmlSanitizerEnabled(resultComponent, element);

        loadContextHelp(resultComponent, element);
        loadIcon(resultComponent, element);
        loadCss(resultComponent, element);
        loadAlign(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadEditable(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadDataLoader(resultComponent, element);
        loadBoolean(element, "autoApply", resultComponent::setAutoApply);

        loadEnum(element, LogicalFilterComponent.Operation.class, "operation",
                resultComponent::setOperation);

        loadInteger(element, "columnsCount", resultComponent::setColumnsCount);
        loadEnum(element, SupportsCaptionPosition.CaptionPosition.class, "captionPosition",
                resultComponent::setCaptionPosition);

        loadBoolean(element, "operationCaptionVisible", resultComponent::setOperationCaptionVisible);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        loadSubFilterComponents(resultComponent, element);
    }

    protected void loadDataLoader(FilterComponent component, Element element) {
        loadString(element, "dataLoader",
                (dataLoaderId) -> {
                    FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
                    ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
                    DataLoader dataLoader = screenData.getLoader(dataLoaderId);
                    component.setDataLoader(dataLoader);

                    getComponentContext().addInitTask((context, window) ->
                            FilterLoaderUtils.updateDataLoaderInitialCondition(((GroupFilterImpl) component),
                                    dataLoader.getCondition())
                    );
                });
    }

    protected void loadSubFilterComponents(GroupFilter component, Element element) {
        for (Element filterElement : element.elements()) {
            ComponentLoader<?> filterComponentLoader = getLayoutLoader().createComponent(filterElement);
            ((FilterComponent) filterComponentLoader.getResultComponent())
                    .setConditionModificationDelegated(true);
            ((FilterComponent) filterComponentLoader.getResultComponent())
                    .setDataLoader(resultComponent.getDataLoader());
            filterComponentLoader.loadComponent();

            component.add((FilterComponent) filterComponentLoader.getResultComponent());
        }
    }
}
