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

package io.jmix.ui.relatedentities;

import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.screen.FrameOwner;
import org.springframework.stereotype.Component;

import java.util.Collection;

//todo implementation
@Component(RelatedEntitiesAPI.NAME)
public class RelatedEntitiesBean implements RelatedEntitiesAPI {

    @Override
    public RelatedEntitiesBuilder builder(FrameOwner frameOwner) {
        return null;
    }

    @Override
    public void openRelatedScreen(Collection<? extends Entity> selectedEntities, MetaClass metaClass, MetaProperty metaProperty) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void openRelatedScreen(Collection<? extends Entity> selectedEntities, MetaClass metaClass, MetaProperty metaProperty, RelatedScreenDescriptor descriptor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends Entity> void openRelatedScreen(Collection<T> selectedEntities, Class<T> clazz, String property) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends Entity> void openRelatedScreen(Collection<T> selectedEntities, Class<T> clazz, String property, RelatedScreenDescriptor descriptor) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
