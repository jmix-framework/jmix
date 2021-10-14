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
import org.springframework.transaction.event.TransactionalEventListener;
import test_support.entity.petclinic.Owner;

import java.util.function.Consumer;

@Component("test_TestComplexListener")
public class TestComplexListener {

    private Consumer<EntityChangedEvent<?>> consumer;

    public void setConsumer(Consumer<EntityChangedEvent<?>> consumer) {
        this.consumer = consumer;
    }

    @TransactionalEventListener
    public void onOrderBeforeCommit(EntityChangedEvent<Owner> event) {
        if (consumer != null) consumer.accept(event);
    }
}
