/*
 * Copyright 2022 Haulmont.
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

package test_support.entity.chained_entity_listeners;

import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("test_ChainedUpdateListener")
public class ChainedUpdateListener {

    @Autowired
    private DataManager dataManager;

    // Don't use `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` because it doesn't chain events
    @EventListener
    public void onEntityOneChanged(EntityChangedEvent<ChainedUpdateEntityOne> event) {
        if (event.getType().equals(EntityChangedEvent.Type.UPDATED)) {
            ChainedUpdateEntityOne entityOne = dataManager.load(event.getEntityId()).one();
            ChainedUpdateEntityTwo entityTwo = dataManager.load(ChainedUpdateEntityTwo.class).query("e.entityOne = ?1", entityOne).one();
            entityTwo.setAmount(entityOne.getAmount());
            dataManager.save(entityTwo);
        }
    }

    @EventListener
    public void onEntityTwoChanged(EntityChangedEvent<ChainedUpdateEntityTwo> event) {
        if (event.getType().equals(EntityChangedEvent.Type.UPDATED)) {
            ChainedUpdateEntityTwo entityTwo = dataManager.load(event.getEntityId()).one();
            ChainedUpdateEntityThree entityThree = dataManager.load(ChainedUpdateEntityThree.class).query("e.entityTwo = ?1", entityTwo).one();
            entityThree.setAmount(entityTwo.getAmount());
            dataManager.save(entityThree);
        }
    }
}
