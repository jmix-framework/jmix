/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.event.EntityChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import java.util.function.BiConsumer;

@Component("test_TestOrdersListener")
public class TestOrdersListener {

    private BiConsumer<EntityChangedEvent, TransactionPhase> consumer;

    public void setConsumer(BiConsumer<EntityChangedEvent, TransactionPhase> consumer) {
        this.consumer = consumer;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOrderBeforeCommit(EntityChangedEvent<Order> event) {
        if (consumer != null) consumer.accept(event, TransactionPhase.BEFORE_COMMIT);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderAfterCommit(EntityChangedEvent<Order> event) {
        if (consumer != null) consumer.accept(event, TransactionPhase.AFTER_COMMIT);
    }


    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOrderLineBeforeCommit(EntityChangedEvent<OrderLine> event) {
        if (consumer != null) consumer.accept(event, TransactionPhase.BEFORE_COMMIT);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderLineAfterCommit(EntityChangedEvent<OrderLine> event) {
        if (consumer != null) consumer.accept(event, TransactionPhase.AFTER_COMMIT);
    }

}
