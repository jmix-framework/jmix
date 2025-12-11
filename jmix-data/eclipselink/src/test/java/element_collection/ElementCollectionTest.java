/*
 * Copyright 2025 Haulmont.
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

package element_collection;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.element_collection.EcAlpha;
import test_support.entity.element_collection.EcBeta;
import test_support.listeners.TestEcAlphaChangedEventListener;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class ElementCollectionTest {

    @Autowired
    EntityStates entityStates;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    TransactionTemplate transactionTemplate;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TestEcAlphaChangedEventListener changedEventListener;

    @AfterEach
    void tearDown() {
        changedEventListener.afterCommitEventConsumer = null;
        changedEventListener.beforeCommitEventConsumer = null;

        jdbc.update("delete from TEST_EC_ALPHA_TAGS");
        jdbc.update("delete from TEST_EC_ALPHA");
        jdbc.update("delete from TEST_EC_BETA");
    }

    @Test
    void testCrudWithEagerLoading() {
        EcAlpha alpha = dataManager.create(EcAlpha.class);
        alpha.setName("foo 1");
        alpha.setTags(List.of("tag1", "tag2"));
        dataManager.saveWithoutReload(alpha);

        EcAlpha loadedAlpha = dataManager.load(EcAlpha.class)
                .id(alpha.getId())
                .fetchPlan(fetchPlanBuilder -> fetchPlanBuilder.addFetchPlan(FetchPlan.BASE).add("tags"))
                .one();
        assertThat(loadedAlpha.getTags()).containsExactly("tag1", "tag2");

        loadedAlpha.getTags().add("tag3");
        dataManager.saveWithoutReload(loadedAlpha);

        loadedAlpha = dataManager.load(EcAlpha.class)
                .id(alpha.getId())
                .fetchPlan(fetchPlanBuilder -> fetchPlanBuilder.addFetchPlan(FetchPlan.BASE).add("tags"))
                .one();
        assertThat(loadedAlpha.getTags()).containsExactly("tag1", "tag2", "tag3");

        List<EcAlpha> list = dataManager.load(EcAlpha.class)
                .all()
                .fetchPlan(fetchPlanBuilder -> fetchPlanBuilder.addFetchPlan(FetchPlan.BASE).add("tags"))
                .list();
        for (EcAlpha alpha1 : list) {
            assertThat(alpha1.getTags()).containsExactly("tag1", "tag2", "tag3");
        }

        dataManager.remove(loadedAlpha);

        List<String> tags = jdbc.queryForList("select TAG from TEST_EC_ALPHA_TAGS where ALPHA_ID = ?", String.class, alpha.getId());
        assertThat(tags).isEmpty();
    }

    @Test
    void testLazyLoading() {
        EcBeta beta = dataManager.create(EcBeta.class);
        beta.setName("beta 1");
        beta = dataManager.save(beta);

        EcAlpha alpha = dataManager.create(EcAlpha.class);
        alpha.setName("foo 1");
        alpha.setBeta(beta);
        alpha.setTags(List.of("tag1", "tag2"));
        dataManager.saveWithoutReload(alpha);

        EcAlpha loadedAlpha = dataManager.load(EcAlpha.class)
                .id(alpha.getId())
                .one();
        assertThat(entityStates.isLoaded(loadedAlpha, "beta")).isFalse();
        assertThat(entityStates.isLoaded(loadedAlpha, "tags")).isFalse();

        List<String> tags = loadedAlpha.getTags();
        assertThat(tags).containsExactly("tag1", "tag2");
        assertThat(entityStates.isLoaded(loadedAlpha, "tags")).isTrue();

        List<EcAlpha> list = dataManager.load(EcAlpha.class)
                .all()
                .list();
        for (EcAlpha alpha1 : list) {
            assertThat(entityStates.isLoaded(alpha1, "tags")).isFalse();

            List<String> tags1 = alpha1.getTags();
            assertThat(tags1).containsExactly("tag1", "tag2");
            assertThat(entityStates.isLoaded(alpha1, "tags")).isTrue();
        }
    }

    @Test
    void testLazyLoadingCollectionAdd() {
        EcAlpha alpha = dataManager.create(EcAlpha.class);
        alpha.setName("foo 1");
        alpha.setTags(List.of("tag1", "tag2"));
        dataManager.saveWithoutReload(alpha);

        EcAlpha loadedAlpha = dataManager.load(EcAlpha.class)
                .id(alpha.getId())
                .one();
        assertThat(entityStates.isLoaded(loadedAlpha, "tags")).isFalse();

        List<String> tags = loadedAlpha.getTags();
        tags.add("tag3");
        assertThat(entityStates.isLoaded(loadedAlpha, "tags")).isTrue();
    }

    @Test
    void testManagedEntity() {
        EcAlpha alpha = dataManager.create(EcAlpha.class);
        alpha.setName("foo 1");
        alpha.setTags(List.of("tag1", "tag2"));
        dataManager.saveWithoutReload(alpha);

        EcAlpha alpha2 = transactionTemplate.execute(status -> {
            EcAlpha alpha1 = entityManager.find(EcAlpha.class, alpha.getId());

            assertThat(entityStates.isLoaded(alpha1, "tags")).isFalse();

            alpha1.getTags().size();
            assertThat(entityStates.isLoaded(alpha1, "tags")).isTrue();
            assertThat(alpha1.getTags()).containsExactly("tag1", "tag2");
            return alpha1;
        });
        assertThat(entityStates.isLoaded(alpha2, "tags")).isTrue();
    }

    @Test
    void testEntityChangedEvents() {
        // create
        setEventConsumer(event -> {
            assertThat(event.getChanges().isChanged("tags")).isTrue();
            assertThat((Object) event.getChanges().getOldValue("tags")).isNull();
        });
        EcAlpha alpha = dataManager.create(EcAlpha.class);
        alpha.setName("foo 1");
        alpha.setTags(List.of("tag1", "tag2"));
        dataManager.saveWithoutReload(alpha);

        // update
        setEventConsumer(event -> {
            assertThat(event.getChanges().isChanged("tags")).isTrue();
            assertThat((Object) event.getChanges().getOldValue("tags")).isEqualTo(List.of("tag1", "tag2"));
        });
        EcAlpha loadedAlpha = dataManager.load(EcAlpha.class)
                .id(alpha.getId())
                .one();
        loadedAlpha.getTags().add("tag3");
        dataManager.saveWithoutReload(loadedAlpha);

        // change another attribute
        setEventConsumer(event -> {
            assertThat(event.getChanges().isChanged("tags")).isFalse();
        });
        loadedAlpha = dataManager.load(EcAlpha.class)
                .id(alpha.getId())
                .one();
        loadedAlpha.setName("foo changed");
        dataManager.saveWithoutReload(loadedAlpha);
    }

    @Test
    void testKeyValueEntity() {
        EcAlpha alpha = dataManager.create(EcAlpha.class);
        alpha.setName("foo 1");
        alpha.setTags(List.of("tag1", "tag2"));
        dataManager.saveWithoutReload(alpha);

        List<KeyValueEntity> list = dataManager.loadValues("select e.name, t from test_EcAlpha e join e.tags t")
                .properties("name", "tag")
                .list();
        KeyValueEntity entity = list.get(0);
        assertThat((Object) entity.getValue("name")).isEqualTo("foo 1");
        assertThat((Object) entity.getValue("tag")).isIn("tag1", "tag2");

        List<String> tags = dataManager.loadValue("select t from test_EcAlpha e join e.tags t", String.class).list();
        assertThat(tags).containsExactlyInAnyOrder("tag1", "tag2");
    }

    private void setEventConsumer(Consumer<EntityChangedEvent<EcAlpha>> eventConsumer) {
        changedEventListener.beforeCommitEventConsumer = eventConsumer;
        changedEventListener.afterCommitEventConsumer = eventConsumer;
    }
}
