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

import io.jmix.core.JmixEntity;
import io.jmix.core.EntityInitializer;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.dynattr.DynamicAttributesState;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component(DynAttrEntityStateInitializer.NAME)
public class DynAttrEntityStateInitializer implements EntityInitializer, Ordered {
    public static final String NAME = "dynattr_DynAttrEntityStateInitializer";

    @Override
    public void initEntity(JmixEntity entity) {
        DynamicAttributesState state = new DynamicAttributesState(entity.__getEntityEntry());
        state.setDynamicAttributes(new DynamicAttributes());
        entity.__getEntityEntry().addExtraState(state);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 50;
    }
}
