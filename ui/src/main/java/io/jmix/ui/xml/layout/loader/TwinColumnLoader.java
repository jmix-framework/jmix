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

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.TwinColumn;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class TwinColumnLoader extends AbstractFieldLoader<TwinColumn> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(TwinColumn.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadOptionsContainer(resultComponent, element);

        loadRows(resultComponent, element);

        loadCaptionProperty(resultComponent, element);
        loadLeftColumnCaption(resultComponent, element);
        loadRightColumnCaption(resultComponent, element);

        loadAddBtnEnabled(resultComponent, element);
        loadReorderable(resultComponent, element);

        loadTabIndex(resultComponent, element);
    }

    protected void loadOptionsContainer(TwinColumn component, Element element) {
        String containerId = element.attributeValue("optionsContainer");
        if (containerId != null) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            InstanceContainer container = screenData.getContainer(containerId);
            if (!(container instanceof CollectionContainer)) {
                throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
            }
            //noinspection unchecked
            component.setOptions(new ContainerOptions((CollectionContainer) container));
        }
    }

    protected void loadRows(TwinColumn resultComponent, Element element) {
        String rows = element.attributeValue("rows");
        if (StringUtils.isNotEmpty(rows)) {
            resultComponent.setRows(Integer.parseInt(rows));
        }
    }

    protected void loadCaptionProperty(TwinColumn resultComponent, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            resultComponent.setOptionCaptionProvider(
                    new CaptionAdapter(captionProperty, applicationContext.getBean(Metadata.class), applicationContext.getBean(MetadataTools.class)));
        }
    }

    protected void loadLeftColumnCaption(TwinColumn resultComponent, Element element) {
        String leftColumnCaption = element.attributeValue("leftColumnCaption");
        if (StringUtils.isNotEmpty(leftColumnCaption)) {
            resultComponent.setLeftColumnCaption(loadResourceString(leftColumnCaption));
        }
    }

    protected void loadRightColumnCaption(TwinColumn resultComponent, Element element) {
        String rightColumnCaption = element.attributeValue("rightColumnCaption");
        if (StringUtils.isNotEmpty(rightColumnCaption)) {
            resultComponent.setRightColumnCaption(loadResourceString(rightColumnCaption));
        }
    }

    protected void loadAddBtnEnabled(TwinColumn resultComponent, Element element) {
        String addBtnEnabled = element.attributeValue("addAllBtnEnabled");
        if (StringUtils.isNotEmpty(addBtnEnabled)) {
            resultComponent.setAddAllBtnEnabled(Boolean.parseBoolean(addBtnEnabled));
        }
    }

    protected void loadReorderable(TwinColumn resultComponent, Element element) {
        String reorderable = element.attributeValue("reorderable");
        if (StringUtils.isNotEmpty(reorderable)) {
            resultComponent.setReorderable(Boolean.parseBoolean(reorderable));
        }
    }
}
