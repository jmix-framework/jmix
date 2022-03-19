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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.RelatedEntities;
import com.haulmont.cuba.gui.components.actions.RelatedAction;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.impl.RelatedEntitiesImpl;
import io.jmix.ui.sys.PropertyOption;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

@Deprecated
public class WebRelatedEntities extends RelatedEntitiesImpl implements RelatedEntities {

    @Override
    public OpenType getOpenType() {
        return OpenType.valueOf(openMode.name());
    }

    @Override
    public void setOpenType(OpenType openType) {
        setOpenMode(openType.getOpenMode());
    }

    @Override
    protected Action createRelatedAction(MetaProperty metaProperty,
                                         @Nullable WindowInfo defaultScreen,
                                         @Nullable PropertyOption propertyOption) {
        RelatedAction relatedAction = RelatedAction.create("related" + actionOrder.size(),
                listComponent, getMetaClass(listComponent), metaProperty);

        relatedAction.setOpenType(getOpenType());

        if (defaultScreen != null) {
            relatedAction.setScreen(defaultScreen.getId());
        }

        if (propertyOption != null) {
            if (StringUtils.isNotEmpty(propertyOption.getCaption())) {
                relatedAction.setCaption(propertyOption.getCaption());
            }
            if (StringUtils.isNotEmpty(propertyOption.getConfigurationName())) {
                relatedAction.setFilterCaption(propertyOption.getConfigurationName());
            }
            if (StringUtils.isNotEmpty(propertyOption.getScreenId())) {
                relatedAction.setScreen(propertyOption.getScreenId());
            }
        }

        return relatedAction;
    }
}
