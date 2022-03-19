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

import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.event.EntityLoadingEvent;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.impl.AfterCompleteTransactionListener;
import io.jmix.data.impl.BeforeCommitTransactionListener;
import io.jmix.data.listener.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import test_support.entity.events.Foo;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;

@Component("test_TestAllDataEventsListener")
public class TestAllDataEventsListener implements
        BeforeCommitTransactionListener,
        AfterCompleteTransactionListener,
        BeforeInsertEntityListener<Foo>,
        BeforeUpdateEntityListener<Foo>,
        BeforeDeleteEntityListener<Foo>,
        AfterInsertEntityListener<Foo>,
        AfterUpdateEntityListener<Foo>,
        AfterDeleteEntityListener<Foo>,
        BeforeDetachEntityListener<Foo>,
        BeforeAttachEntityListener<Foo> {

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

    public static List<Info> entityChangedEvents = new ArrayList<>();

    public static List<EventInfo> allEvents = new ArrayList<>();

    @Autowired
    private DataSource dataSource;

    public static void clear() {
        allEvents.clear();
        entityChangedEvents.clear();
    }

    @EventListener
    void loading(EntityLoadingEvent<Foo> event) {
        allEvents.add(new EventInfo("EntityLoadingEvent", event));
    }

    @EventListener
    void saving(EntitySavingEvent<Foo> event) {
        allEvents.add(new EventInfo("EntitySavingEvent: isNew=" + event.isNewEntity(), event));
    }

    @EventListener
    void beforeCommit(EntityChangedEvent<Foo> event) {
        allEvents.add(new EventInfo("EntityChangedEvent: beforeCommit, " + event.getType(), event));
        entityChangedEvents.add(new Info(event, isCommitted(event.getEntityId())));
    }

    @TransactionalEventListener
    void afterCommit(EntityChangedEvent<Foo> event) {
        allEvents.add(new EventInfo("EntityChangedEvent: afterCommit, " + event.getType(), event));
        entityChangedEvents.add(new Info(event, isCommitted(event.getEntityId())));
    }

    @Override
    public void beforeCommit(String storeName, Collection<Object> managedEntities) {
        allEvents.add(new EventInfo("BeforeCommitTransactionListener", managedEntities));
    }

    @Override
    public void afterComplete(boolean committed, Collection<Object> detachedEntities) {
        allEvents.add(new EventInfo("AfterCompleteTransactionListener", new Object[]{committed, detachedEntities}));
    }

    @Override
    public void onAfterDelete(Foo entity) {
        allEvents.add(new EventInfo("AfterDeleteEntityListener", entity));
    }

    @Override
    public void onAfterInsert(Foo entity) {
        allEvents.add(new EventInfo("AfterInsertEntityListener", entity));
    }

    @Override
    public void onAfterUpdate(Foo entity) {
        allEvents.add(new EventInfo("AfterUpdateEntityListener", entity));
    }

    @Override
    public void onBeforeAttach(Foo entity) {
        allEvents.add(new EventInfo("BeforeAttachEntityListener", entity));
    }

    @Override
    public void onBeforeDelete(Foo entity) {
        allEvents.add(new EventInfo("BeforeDeleteEntityListener", entity));
    }

    @Override
    public void onBeforeDetach(Foo entity) {
        allEvents.add(new EventInfo("BeforeDetachEntityListener", entity));
    }

    @Override
    public void onBeforeInsert(Foo entity) {
        allEvents.add(new EventInfo("BeforeInsertEntityListener", entity));
    }

    @Override
    public void onBeforeUpdate(Foo entity) {
        allEvents.add(new EventInfo("BeforeUpdateEntityListener", entity));
    }

    private boolean isCommitted(Id<Foo> entityId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            List<Map<String, Object>> row = jdbcTemplate.queryForList("select id from TEST_EVENTS_FOO where id = ?",
                    entityId.getValue().toString());
            return !row.isEmpty();
        });
        try {
            return future.get(200L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            return false;
        }
    }
}
