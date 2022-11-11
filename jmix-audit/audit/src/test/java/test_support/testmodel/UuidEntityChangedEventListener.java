/*
 * Copyright (c) 2008-2018 Haulmont.
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

package test_support.testmodel;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("test_UuidEntityChangedEventListener")
public class UuidEntityChangedEventListener {

    public static boolean enabled = false;

    @Autowired
    private DataManager dataManager;

    @EventListener
    void beforeCommit(EntityChangedEvent<UuidEntity> event) {
        if (enabled) {
            if (event.getType() == EntityChangedEvent.Type.CREATED) {
                UuidEntity entity = dataManager.load(event.getEntityId())
                        .fetchPlan(FetchPlan.BASE)
                        .optional()
                        .orElse(null);

                if (entity != null) {
                    entity.setDescription(entity.getName());
                    dataManager.save(entity);
                }
            }
        }
    }
}
