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

package io.jmix.dynattrflowui.facet;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.EventHub;
import io.jmix.dynattrflowui.impl.AttributeDefaultValues;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.impl.AbstractFacet;
import io.jmix.ui.screen.MasterDetailScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.UiControllerUtils;

import javax.annotation.Nullable;

@Internal
public class DynAttrFacetImpl extends AbstractFacet implements DynAttrFacet {
    protected AttributeDefaultValues attributeDefaultValues;

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);
        subscribe();
    }

    public void setAttributeDefaultValues(AttributeDefaultValues attributeDefaultValues) {
        this.attributeDefaultValues = attributeDefaultValues;
    }

    private void subscribe() {
        Frame frame = getOwner();
        if (frame == null) {
            throw new IllegalStateException("DynAttrFacet is not attached to Frame");
        }

        Screen screen = UiControllerUtils.getScreen(frame.getFrameOwner());
        if (screen instanceof StandardEditor) {
            EventHub screenEvents = UiControllerUtils.getEventHub(screen);
            screenEvents.subscribe(StandardEditor.InitEntityEvent.class, this::initEntityInStandardEditor);
        } else if (screen instanceof MasterDetailScreen) {
            EventHub screenEvents = UiControllerUtils.getEventHub(screen);
            screenEvents.subscribe(MasterDetailScreen.InitEntityEvent.class, this::initEntityInMasterDetailScreen);
        }
    }

    protected void initEntityInStandardEditor(StandardEditor.InitEntityEvent<?> e) {
        attributeDefaultValues.initDefaultAttributeValues(e.getEntity());
    }

    protected void initEntityInMasterDetailScreen(MasterDetailScreen.InitEntityEvent<?> e) {
        attributeDefaultValues.initDefaultAttributeValues(e.getEntity());
    }
}
