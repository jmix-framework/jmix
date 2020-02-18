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

package test_support.entity_listener;

import io.jmix.data.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import test_support.entity.sales.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TestEventListener {

    public static List<EntityChangedEvent<Customer, UUID>> customerEvents = new ArrayList<>();

    @EventListener
    void onCustomerChanged(EntityChangedEvent<Customer, UUID> event) {
        System.out.println("onCustomerChanged: " + event);
        customerEvents.add(event);
    }
}
