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

package io.jmix.dynattr.impl;

import io.jmix.core.EntityInitializer;
import io.jmix.core.JmixOrder;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.dynattr.DynamicAttributesState;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import static io.jmix.core.entity.EntitySystemAccess.addExtraState;
import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;

@Component("dynat_DynAttrEntityStateInitializer")
public class DynAttrEntityStateInitializer implements EntityInitializer, Ordered {

    @Override
    public void initEntity(Object entity) {
        DynamicAttributesState state = new DynamicAttributesState(getEntityEntry(entity));
        state.setDynamicAttributes(new DynamicAttributes());
        addExtraState(entity, state);
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 50;
    }
}
