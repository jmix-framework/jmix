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

package changelog;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs the module changelog on its own empty in-memory database, apart from the Spring contexts of the other tests
 * that create their tables from the entities.
 */
class KeepInContextChangelogTest {

    private static final String CHANGELOG = "io/jmix/aitoolsflowuidata/liquibase/changelog.xml";

    /**
     * Number of change sets that precede the one adding {@code KEEP_IN_CONTEXT} (001: 1, 002: 2, 003: 1).
     */
    private static final int CHANGE_SETS_BEFORE_KEEP_IN_CONTEXT = 4;

    @Test
    void changelog_emptyDatabase_addsNotNullColumnDefaultingToFalse() throws Exception {
        JdbcTemplate jdbc = newDatabase();
        updateTo(jdbc, null);

        UUID conversationId = insertConversation(jdbc);
        UUID messageId = insertMessageWithoutKeepInContext(jdbc, conversationId);

        assertEquals(Boolean.FALSE, keepInContextOf(jdbc, messageId));
        assertEquals("NO", jdbc.queryForObject("select is_nullable from information_schema.columns "
                + "where table_name = 'AITLS_AI_CHAT_MESSAGE_ENTITY' and column_name = 'KEEP_IN_CONTEXT'",
                String.class));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "update AITLS_AI_CHAT_MESSAGE_ENTITY set KEEP_IN_CONTEXT = null where ID = ?", messageId));
    }

    @Test
    void changelog_existingMessages_getFalse() throws Exception {
        JdbcTemplate jdbc = newDatabase();
        updateTo(jdbc, CHANGE_SETS_BEFORE_KEEP_IN_CONTEXT);
        // Guards against a wrong change set count, which would make the test pass vacuously.
        assertEquals(0, jdbc.queryForObject("select count(*) from information_schema.columns "
                + "where table_name = 'AITLS_AI_CHAT_MESSAGE_ENTITY' and column_name = 'KEEP_IN_CONTEXT'",
                Integer.class));
        UUID messageId = insertMessageWithoutKeepInContext(jdbc, insertConversation(jdbc));

        updateTo(jdbc, null);

        assertEquals(Boolean.FALSE, keepInContextOf(jdbc, messageId));
    }

    private JdbcTemplate newDatabase() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:hsqldb:mem:aitls-changelog-" + UUID.randomUUID(), "sa", "");
        return new JdbcTemplate(dataSource);
    }

    private void updateTo(JdbcTemplate jdbc, @Nullable Integer changeSetsToApply) throws Exception {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), database);
            if (changeSetsToApply == null) {
                liquibase.update("");
            } else {
                liquibase.update(changeSetsToApply, "");
            }
        }
    }

    private UUID insertConversation(JdbcTemplate jdbc) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into AITLS_AI_CONVERSATION_ENTITY (ID, TITLE, USERNAME) values (?, ?, ?)",
                id, "Title", "user");
        return id;
    }

    private UUID insertMessageWithoutKeepInContext(JdbcTemplate jdbc, UUID conversationId) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into AITLS_AI_CHAT_MESSAGE_ENTITY (ID, CONVERSATION_ID, TYPE_) values (?, ?, ?)",
                id, conversationId, "USER");
        return id;
    }

    private Boolean keepInContextOf(JdbcTemplate jdbc, UUID messageId) {
        return jdbc.queryForObject("select KEEP_IN_CONTEXT from AITLS_AI_CHAT_MESSAGE_ENTITY where ID = ?",
                Boolean.class, messageId);
    }
}
