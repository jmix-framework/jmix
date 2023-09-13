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
import io.jmix.flowui.facet.impl.AbstractFacet;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import jakarta.annotation.Nullable;


@Internal
public class DynAttrFacetImpl extends AbstractFacet implements DynAttrFacet {
    protected AttributeDefaultValues attributeDefaultValues;

    @Override
    public void setOwner(@Nullable View owner) {
        super.setOwner(owner);
        subscribe();
    }

    public void setAttributeDefaultValues(AttributeDefaultValues attributeDefaultValues) {
        this.attributeDefaultValues = attributeDefaultValues;
    }

    private void subscribe() {
        View<?> view = getOwner();
        if (view == null) {
            throw new IllegalStateException("DynAttrFacet is not attached to Frame");
        }

        if (view instanceof StandardDetailView<?>) {
            ViewControllerUtils.addInitEntityEvent((StandardDetailView)view, e -> attributeDefaultValues.initDefaultAttributeValues(e.getEntity()));
        }
    }
}
