/*
 * Copyright 2026 Haulmont.
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

package test_support.listeners;

import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import test_support.entity.events.on_delete_stack_overflow.History;
import test_support.entity.events.on_delete_stack_overflow.Payment;
import test_support.entity.events.on_delete_stack_overflow.Product;

import java.util.UUID;


@Component
public class OnDeleteStackOverflowListener {
    @Autowired
    private DataManager dataManager;

    @EventListener
    public void onPaymentChangedBeforeCommit(final EntityChangedEvent<Payment> event) {
        History history = dataManager.create(History.class);
        history.setObjId((UUID) event.getEntityId().getValue());
        history.setEvent(event.toString());
        history.setEventType(event.getType().toString());
        dataManager.save(history);
    }

    @EventListener
    public void onProductChangedBeforeCommit(final EntityChangedEvent<Product> event) {
        History history = dataManager.create(History.class);
        history.setObjId((UUID) event.getEntityId().getValue());
        history.setEvent(event.toString());
        history.setEventType(event.getType().toString());
        dataManager.save(history);
    }
}