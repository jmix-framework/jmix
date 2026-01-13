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

import com.vaadin.flow.component.Composite;
import io.jmix.core.annotation.Internal;
import io.jmix.dynattrflowui.impl.AttributeDefaultValues;
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.facet.impl.AbstractFacet;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.lang.Nullable;

@Internal
public class DynAttrFacetImpl extends AbstractFacet implements DynAttrFacet {

    protected AttributeDefaultValues attributeDefaultValues;

    public DynAttrFacetImpl(AttributeDefaultValues attributeDefaultValues) {
        this.attributeDefaultValues = attributeDefaultValues;
    }

    @Override
    public <T extends Composite<?> & FacetOwner> void setOwner(@Nullable T owner) {
        super.setOwner(owner);
        subscribe();
    }

    protected void subscribe() {
        FacetOwner owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("%s is not attached to owner"
                    .formatted(DynAttrFacet.class.getSimpleName()));
        }

        if (owner instanceof StandardDetailView<?> view) {
            ViewControllerUtils.addInitEntityEventListener(view,
                    e -> attributeDefaultValues.initDefaultAttributeValues(e.getEntity()));
        }
    }
}
