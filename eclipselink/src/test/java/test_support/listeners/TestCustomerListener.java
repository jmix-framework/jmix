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

package test_support.listeners;

import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.listener.BeforeDetachEntityListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Status;

import java.util.function.Consumer;

@Component("test_TestCustomerListener")
public class TestCustomerListener implements BeforeDetachEntityListener<Customer> {

    public Consumer<EntityChangedEvent<Customer>> changedEventConsumer;

    public Consumer<Customer> beforeDetachConsumer;

    public Consumer<EntityChangedEvent<Customer>> getChangedEventConsumer() {
        return changedEventConsumer;
    }

    public void setChangedEventConsumer(Consumer<EntityChangedEvent<Customer>> changedEventConsumer) {
        this.changedEventConsumer = changedEventConsumer;
    }

    public Consumer<Customer> getBeforeDetachConsumer() {
        return beforeDetachConsumer;
    }

    public void setBeforeDetachConsumer(Consumer<Customer> beforeDetachConsumer) {
        this.beforeDetachConsumer = beforeDetachConsumer;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onCustomerChanged(EntityChangedEvent<Customer> event) {
        if (changedEventConsumer != null) {
            changedEventConsumer.accept(event);
        }
    }

    @EventListener
    public void onCustomerCreate(EntitySavingEvent<Customer> event) {
        if (event.getEntity().getStatus() == null) {
            event.getEntity().setStatus(Status.OK);
        }
    }

    @Override
    public void onBeforeDetach(Customer entity) {
        if (beforeDetachConsumer != null) {
            beforeDetachConsumer.accept(entity);
        }
    }
}
