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
import io.jmix.ui.Actions;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.impl.GuiActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class EntityPickerLoader extends AbstractValuePickerLoader<EntityPicker> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(EntityPicker.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadCaptionProperty(resultComponent, element);
        loadMetaClass(resultComponent, element);
    }

    protected void loadCaptionProperty(EntityPicker resultComponent, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            resultComponent.setOptionCaptionProvider(
                    new CaptionAdapter(captionProperty, applicationContext.getBean(Metadata.class), applicationContext.getBean(MetadataTools.class)));
        }
    }

    protected boolean createActionsByMetaAnnotations() {
        return getGuiActionSupport().createActionsByMetaAnnotations(resultComponent);
    }

    protected GuiActionSupport getGuiActionSupport() {
        return (GuiActionSupport) applicationContext.getBean(GuiActionSupport.NAME);
    }

    protected void loadMetaClass(EntityPicker resultComponent, Element element) {
        String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            resultComponent.setMetaClass(getMetadata().getClass(metaClass));
        }
    }

    protected Metadata getMetadata() {
        return (Metadata) applicationContext.getBean(Metadata.NAME);
    }

    @Override
    protected void addDefaultActions() {
        boolean actionsByMetaAnnotations = createActionsByMetaAnnotations();
        if (!actionsByMetaAnnotations) {
            addEntityPickerDefaultActions();
        }
    }

    protected void addEntityPickerDefaultActions() {
        Actions actions = getActions();

        resultComponent.addAction(actions.create(LookupAction.ID));
        resultComponent.addAction(actions.create(EntityClearAction.ID));
    }
}
