/*
 * Copyright 2020 Haulmont.
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

import com.google.common.base.Strings;
import io.jmix.core.event.EntityLoadingEvent;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import test_support.entity.events.Bar;

@Component("test_TestLoadSaveEventListener")
public class TestLoadSaveEventListener {

    @EventListener
    void loading(EntityLoadingEvent<Bar> event) {
        Bar bar = event.getEntity();
        bar.setDescription(bar.getName() + ":" + bar.getAmount());
    }

    @EventListener
    void saving(EntitySavingEvent<Bar> event) {
        Bar bar = event.getEntity();

        if (event.isNewEntity()) {
            if (bar.getName() == null) {
                bar.setName("new");
            }
            if (bar.getAmount() == null) {
                bar.setAmount(1);
            }
        }

        if (!Strings.isNullOrEmpty(bar.getDescription()) && bar.getDescription().contains(":")) {
            String[] strings = bar.getDescription().split(":");
            bar.setName(strings[0]);
            bar.setAmount(Integer.valueOf(strings[1]));
        }
    }
}
