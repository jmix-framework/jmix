/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchdynattr.listener;

import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.dynattr.model.CategoryAttributeValue;
import io.jmix.search.listener.EntityTrackingListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component("search_dynattr_support_DynAttrAnnotatedIndexDefinitionProcessor")
public class DynAttrEntityTrackingListener extends EntityTrackingListener {
    @Override
    public void onEntityChangedBeforeCommit(EntityChangedEvent<?> event) {
        Optional<?> obj = dataManager.load(event.getEntityId()).optional();
        if (obj.isPresent() && obj.get() instanceof CategoryAttributeValue categoryAttributeValue) {
            Class<Object> javaClass = metadata.getClass(categoryAttributeValue.getCategoryAttribute().getCategoryEntityType()).getJavaClass();
            indexingQueueManager.enqueueIndexByEntityId(Id.of(categoryAttributeValue.getObjectEntityId(), javaClass));
            return;
        }

        super.onEntityChangedBeforeCommit(event);
    }
}
