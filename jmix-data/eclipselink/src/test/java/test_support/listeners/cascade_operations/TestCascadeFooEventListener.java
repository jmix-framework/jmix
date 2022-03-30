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

package test_support.listeners.cascade_operations;

import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.event.EntityLoadingEvent;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.listener.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import test_support.entity.cascade_operations.JpaCascadeFoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("test_TestCascadeFooEventListener")
public class TestCascadeFooEventListener implements

        BeforeInsertEntityListener<JpaCascadeFoo>,
        AfterInsertEntityListener<JpaCascadeFoo>,

        BeforeUpdateEntityListener<JpaCascadeFoo>,
        AfterUpdateEntityListener<JpaCascadeFoo>,

        BeforeDeleteEntityListener<JpaCascadeFoo>,
        AfterDeleteEntityListener<JpaCascadeFoo>,

        BeforeDetachEntityListener<JpaCascadeFoo>,
        BeforeAttachEntityListener<JpaCascadeFoo> {

    public static class EventInfo {
        public final String message;
        public final Object[] payload;

        public EventInfo(String message, Object payload) {
            this.message = message;
            this.payload = new Object[]{payload};
        }

        public EventInfo(String message, Object[] payload) {
            this.message = message;
            this.payload = payload;
        }

        @Override
        public String toString() {
            return message + " { " + (payload == null ? null : Arrays.asList(payload)) + " }";
        }
    }

    public static class Info {
        public final EntityChangedEvent event;
        public final boolean committedToDb;

        public Info(EntityChangedEvent event, boolean committedToDb) {
            this.event = event;
            this.committedToDb = committedToDb;
        }
    }

    public static List<EventInfo> allEvents = new ArrayList<>();

    public static void clear() {
        allEvents.clear();
    }

    @EventListener
    void loading(EntityLoadingEvent<JpaCascadeFoo> event) {
        allEvents.add(new EventInfo("EntityLoadingEvent", event));
    }

    @EventListener
    void saving(EntitySavingEvent<JpaCascadeFoo> event) {
        allEvents.add(new EventInfo("EntitySavingEvent: isNew=" + event.isNewEntity(), event));
    }

    @EventListener
    void beforeCommit(EntityChangedEvent<JpaCascadeFoo> event) {
        allEvents.add(new EventInfo("EntityChangedEvent: beforeCommit, " + event.getType(), event));
    }

    @TransactionalEventListener
    void afterCommit(EntityChangedEvent<JpaCascadeFoo> event) {
        allEvents.add(new EventInfo("EntityChangedEvent: afterCommit, " + event.getType(), event));
    }

    @Override
    public void onAfterDelete(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("AfterDeleteEntityListener", entity));
    }

    @Override
    public void onAfterInsert(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("AfterInsertEntityListener", entity));
    }

    @Override
    public void onAfterUpdate(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("AfterUpdateEntityListener", entity));
    }

    @Override
    public void onBeforeAttach(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("BeforeAttachEntityListener", entity));
    }

    @Override
    public void onBeforeDelete(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("BeforeDeleteEntityListener", entity));
    }

    @Override
    public void onBeforeDetach(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("BeforeDetachEntityListener", entity));
    }

    @Override
    public void onBeforeInsert(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("BeforeInsertEntityListener", entity));
    }

    @Override
    public void onBeforeUpdate(JpaCascadeFoo entity) {
        allEvents.add(new EventInfo("BeforeUpdateEntityListener", entity));
    }
}
