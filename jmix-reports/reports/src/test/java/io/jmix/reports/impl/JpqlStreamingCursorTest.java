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

package io.jmix.reports.impl;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.data.StoreAwareLocator;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.libintegration.JpqlDataLoader;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;
import io.jmix.reports.test_support.entity.GameTitle;
import io.jmix.reports.test_support.entity.Publisher;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class JpqlStreamingCursorTest {

    @Autowired
    protected JpqlDataLoader jpqlDataLoader;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from GAME_TITLE");
        jdbcTemplate.update("delete from PUBLISHER");
    }

    @Test
    void testStreamsScalarRowsFromCursor() {
        for (int i = 0; i < 25; i++) {
            Publisher publisher = metadata.create(Publisher.class);
            publisher.setName("P" + String.format("%02d", i));
            dataManager.unconstrained().save(publisher);
        }

        DataSet query = dataSet("select p.name as \"pubName\" from Publisher p order by p.name");

        List<Map<String, Object>> collected = new ArrayList<>();
        Integer count = jpqlDataLoader.loadDataStreaming(query, null, Map.of(), rows -> {
            rows.forEachRemaining(collected::add);
            return collected.size();
        });

        assertThat(count).isEqualTo(25);
        assertThat(collected.get(0).get("pubName")).isEqualTo("P00");
        assertThat(collected.get(24).get("pubName")).isEqualTo("P24");
    }

    @Test
    void testStreamingRunsInReadOnlyTransaction() {
        Publisher publisher = metadata.create(Publisher.class);
        publisher.setName("RO");
        dataManager.unconstrained().save(publisher);

        DataSet query = dataSet("select p.name as \"pubName\" from Publisher p");

        Boolean[] readOnly = new Boolean[1];
        List<Map<String, Object>> collected = new ArrayList<>();
        jpqlDataLoader.loadDataStreaming(query, null, Map.of(), rows -> {
            readOnly[0] = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            rows.forEachRemaining(collected::add);
            return null;
        });

        assertThat(readOnly[0]).as("streaming render runs in a read-only transaction").isTrue();
        assertThat(collected).hasSize(1);
    }

    @Test
    void testStreamsEntityRowsAsEntityMaps() {
        Publisher publisher = metadata.create(Publisher.class);
        publisher.setName("Solo");
        dataManager.unconstrained().save(publisher);

        DataSet query = dataSet("select p from Publisher p");

        List<Map<String, Object>> collected = new ArrayList<>();
        jpqlDataLoader.loadDataStreaming(query, null, Map.of(), rows -> {
            rows.forEachRemaining(collected::add);
            return null;
        });

        assertThat(collected).hasSize(1);
        assertThat(collected.get(0).get("name")).isEqualTo("Solo");
    }

    @Test
    void testReportingExceptionsFromRenderCallbackPropagateUnwrapped() {
        DataSet query = dataSet("select p from Publisher p");

        ReportingInterruptedException e = assertThrows(ReportingInterruptedException.class,
                () -> jpqlDataLoader.loadDataStreaming(query, null, Map.of(), rows -> {
                    throw new ReportingInterruptedException("cancelled by user");
                }));
        assertThat(e.getMessage()).isEqualTo("cancelled by user");
    }

    @Test
    void testPersistenceContextIsClearedPeriodicallyWhileStreamingEntities() {
        for (int i = 0; i < 10; i++) {
            Publisher publisher = metadata.create(Publisher.class);
            publisher.setName("C" + String.format("%02d", i));
            dataManager.unconstrained().save(publisher);
        }

        DataSet query = dataSet("select p from Publisher p order by p.name");

        jpqlDataLoader.setCursorClearInterval(3);
        try {
            Integer managedAtEnd = jpqlDataLoader.loadDataStreaming(query, null, Map.of(), rows -> {
                rows.forEachRemaining(row -> {
                });
                return storeAwareLocator.getEntityManager(Stores.MAIN)
                        .unwrap(UnitOfWorkImpl.class).getCloneMapping().size();
            });

            // Tight bound: with clear-interval 3 over 10 rows the persistence context is evicted
            // repeatedly, so at most one clear-interval worth of managed clones remains at the end.
            assertThat(managedAtEnd).isLessThanOrEqualTo(3);
        } finally {
            jpqlDataLoader.setCursorClearInterval(1000);
        }
    }

    @Test
    void testLazyReferenceAttributeStaysReadableAfterCursorClear() {
        for (int i = 0; i < 6; i++) {
            Publisher publisher = metadata.create(Publisher.class);
            publisher.setName("Pub" + String.format("%02d", i));
            dataManager.unconstrained().save(publisher);

            GameTitle game = metadata.create(GameTitle.class);
            game.setName("Game" + String.format("%02d", i));
            game.setReleaseDate(LocalDate.of(2020, 1, 1));
            game.setPublisher(publisher);
            dataManager.unconstrained().save(game);
        }

        DataSet query = dataSet("select g from GameTitle g order by g.name");

        // A small fetch size and clear interval force several cursor pages and persistence-context clears
        // across the 6 rows, so most rows are produced after at least one clear.
        jpqlDataLoader.setStreamingFetchSize(2);
        jpqlDataLoader.setCursorClearInterval(2);
        List<Object> publisherNames = new ArrayList<>();
        try {
            jpqlDataLoader.loadDataStreaming(query, null, Map.of(), rows -> {
                while (rows.hasNext()) {
                    Map<String, Object> row = rows.next();
                    // Resolve the lazy reference the moment the row is produced, as the formatter does.
                    publisherNames.add(row.get("publisher.name"));
                }
                return null;
            });
        } finally {
            jpqlDataLoader.setStreamingFetchSize(1000);
            jpqlDataLoader.setCursorClearInterval(1000);
        }

        // The lazy publisher.name stays resolvable for every row even after the persistence context is
        // cleared, because the streaming transaction is held open for the whole render: em.clear() only
        // bounds the managed-clone growth, it does not break lazy loading through the live session.
        assertThat(publisherNames)
                .containsExactly("Pub00", "Pub01", "Pub02", "Pub03", "Pub04", "Pub05");
    }

    @Test
    void testNonStreamingExecutionOfTheSameJpqlIsUnaffectedByAPriorStreamingRun() {
        for (int i = 0; i < 5; i++) {
            Publisher publisher = metadata.create(Publisher.class);
            publisher.setName("P" + i);
            dataManager.unconstrained().save(publisher);
        }
        String script = "select p.name as \"pubName\" from Publisher p order by p.name";

        // Streaming configures a cursor on the native EclipseLink query obtained from getDatabaseQuery();
        // if that were the session's shared parsed query, the cursor policy would leak into later executions.
        Integer streamed = jpqlDataLoader.loadDataStreaming(dataSet(script), null, Map.of(), rows -> {
            int n = 0;
            while (rows.hasNext()) {
                rows.next();
                n++;
            }
            return n;
        });
        assertThat(streamed).isEqualTo(5);

        // The same JPQL executed non-streaming afterwards must behave normally: a plain result list,
        // not a leaked cursor policy.
        List<Map<String, Object>> rows = jpqlDataLoader.loadData(dataSet(script), null, Map.of());

        assertThat(rows).hasSize(5);
        assertThat(rows.get(0).get("pubName")).isEqualTo("P0");
        assertThat(rows.get(4).get("pubName")).isEqualTo("P4");
    }

    protected DataSet dataSet(String script) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("q");
        dataSet.setType(DataSetType.JPQL);
        dataSet.setText(script);
        return dataSet;
    }
}
