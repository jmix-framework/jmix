/*
 * Copyright 2016 Haulmont.
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

package io.jmix.samples.rest.listeners;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.data.ReferenceIdProvider;
import io.jmix.data.listener.BeforeAttachEntityListener;
import io.jmix.data.listener.BeforeDetachEntityListener;
import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("jmix_CarDetachListener")
public class CarDetachListener implements BeforeDetachEntityListener<Car>, BeforeAttachEntityListener<Car> {

    @Autowired
    private ReferenceIdProvider referenceIdProvider;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private DataManager dataManager;

    @Override
    public void onBeforeDetach(Car entity) {
        // This is for testing the listener only. Usage of persistenceTools.getReferenceId() does not make sense here.
        ReferenceIdProvider.RefId refId = referenceIdProvider.getReferenceId(entity, "currency");
        if (refId.isLoaded() && entityStates.isLoaded(entity, "currencyCode")) {
            entity.setCurrencyCode((String) refId.getValue());
        }
    }

    @Override
    public void onBeforeAttach(Car entity) {
        if (entityStates.isLoaded(entity, "currency") && entity.getCurrency() == null && entity.getCurrencyCode() != null) {
            Currency currency = dataManager.load(Currency.class)
                    .query("select c from ref_Currency c where c.code = :code")
                    .parameter("code", entity.getCurrencyCode())
                    .one();
            entity.setCurrency(currency);
        }
    }
}
