/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.listener;

import com.haulmont.cuba.core.model.common.Server;
import com.haulmont.cuba.core.EntityManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("cuba_TestDetachAttachListener")
public class TestDetachAttachListener implements
        BeforeDetachEntityListener<Server>, BeforeAttachEntityListener<Server> {

    public final List<String> events = new ArrayList<>();

    @Override
    public void onBeforeAttach(Server entity) {
        events.add("onBeforeAttach: " + entity.getId());
    }

    @Override
    public void onBeforeDetach(Server entity, EntityManager entityManager) {
        events.add("onBeforeDetach: " + entity.getId());
    }
}
