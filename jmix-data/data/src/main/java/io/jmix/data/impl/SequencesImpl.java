/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.data.impl;

import com.google.common.base.Preconditions;
import io.jmix.core.Stores;
import io.jmix.data.Sequence;
import io.jmix.data.persistence.SequenceSupport;
import io.jmix.data.Sequences;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.persistence.DbmsSpecifics;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

@Component("data_Sequences")
public class SequencesImpl implements Sequences {

    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected DbmsSpecifics dbmsSpecifics;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    protected Set<String> existingSequences = ConcurrentHashMap.newKeySet();

    protected static final Pattern SEQ_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    protected static final Object NO_RESULT = new Object();

    @Override
    public long createNextValue(Sequence sequence) {
        Preconditions.checkNotNull(sequence, "Sequence can't be null");
        checkSequenceName(sequence.getName());
        String sqlScript = getSequenceSupport(sequence).getNextValueSql(sequence.getName());
        return getResult(sequence, sqlScript);
    }

    @Override
    public long getCurrentValue(Sequence sequence) {
        Preconditions.checkNotNull(sequence, "Sequence can't be null");
        checkSequenceName(sequence.getName());
        String sqlScript = getSequenceSupport(sequence).getCurrentValueSql(sequence.getName());
        return getResult(sequence, sqlScript);
    }

    @Override
    public void setCurrentValue(Sequence sequence, long value) {
        Preconditions.checkNotNull(sequence, "Sequence can't be null");
        checkSequenceName(sequence.getName());
        String sqlScript = getSequenceSupport(sequence).modifySequenceSql(sequence.getName(), value);
        lock.readLock().lock();
        try {
            TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(getDataStore(sequence));
            transactionTemplate.executeWithoutResult(status -> {
                checkSequenceExists(sequence);
                executeScript(sequence, sqlScript);
            });
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteSequence(Sequence sequence) {
        Preconditions.checkNotNull(sequence, "Sequence can't be null");
        checkSequenceName(sequence.getName());
        String sequenceName = sequence.getName();
        if (!existingSequences.contains(sequenceName)) {
            throw new IllegalStateException(String.format("Attempt to delete nonexistent sequence '%s'", sequence));
        }
        String sqlScript = getSequenceSupport(sequence).deleteSequenceSql(sequenceName);
        lock.writeLock().lock();
        try {
            if (!existingSequences.contains(sequenceName)) {
                return;
            }
            TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(getDataStore(sequence));
            transactionTemplate.executeWithoutResult(status -> {
                executeScript(sequence, sqlScript);
                existingSequences.remove(sequenceName);
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * INTERNAL. Used by tests.
     */
    public void reset() {
        existingSequences.clear();
    }

    protected long getResult(Sequence sequence, String sqlScript) {
        lock.readLock().lock();
        try {
            TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(getDataStore(sequence));
            Object value = transactionTemplate.execute((TransactionCallback<?>) status -> {
                checkSequenceExists(sequence);
                return executeScript(sequence, sqlScript);
            });
            if (value instanceof Long)
                return (Long) value;
            else if (value instanceof BigDecimal)
                return ((BigDecimal) value).longValue();
            else if (value instanceof BigInteger)
                return ((BigInteger) value).longValue();
            else if (value instanceof String)
                return Long.parseLong((String) value);
            else if (value == null)
                throw new IllegalStateException("No value returned");
            else
                throw new IllegalStateException("Unsupported value type: " + value.getClass());
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void checkSequenceExists(Sequence sequence) {
        String sequenceName = sequence.getName();
        if (existingSequences.contains(sequenceName)) {
            return;
        }

        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
            try {
                // Create sequence in separate transaction because it's name is cached and we want to be sure it is created
                // regardless of possible errors in the invoking code
                String storeName = getDataStore(sequence);
                TransactionTemplate transactionTemplate = new TransactionTemplate(storeAwareLocator.getTransactionManager(storeName));
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionTemplate.executeWithoutResult(status -> {
                    EntityManager entityManager = storeAwareLocator.getEntityManager(storeName);
                    SequenceSupport sequenceSupport = getSequenceSupport(sequence);
                    Query query = entityManager.createNativeQuery(sequenceSupport.sequenceExistsSql(sequenceName));
                    List list = query.getResultList();
                    if (list.isEmpty()) {
                        query = entityManager.createNativeQuery(
                                sequenceSupport.createSequenceSql(sequenceName, sequence.getStartValue(), sequence.getIncrement()));
                        query.executeUpdate();
                    }
                    existingSequences.add(sequenceName);
                });
            } finally {
                lock.readLock().lock();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected Object executeScript(Sequence sequence, String sqlScript) {
        JdbcTemplate jdbcTemplate = storeAwareLocator.getJdbcTemplate(getDataStore(sequence));

        StringTokenizer tokenizer = new StringTokenizer(sqlScript, SequenceSupport.SQL_DELIMITER);
        Object value = null;
        while (tokenizer.hasNext()) {
            String sql = tokenizer.nextToken();
            try {
                Object result = jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
                    if (ps.execute()) {
                        ResultSet rs = ps.getResultSet();
                        if (rs.next())
                            return rs.getLong(1);
                    }
                    return NO_RESULT;
                });
                if (result != NO_RESULT) {
                    value = result;
                }
            } catch (DataAccessException e) {
                throw new IllegalStateException("Error executing SQL for getting next number", e);
            }
        }
        return value;
    }


    protected SequenceSupport getSequenceSupport(Sequence sequence) {
        return dbmsSpecifics.getSequenceSupport(getDataStore(sequence));
    }

    protected String getDataStore(Sequence sequence) {
        return sequence.getDataStore() == null ? Stores.MAIN : sequence.getDataStore();
    }

    protected void checkSequenceName(String sequenceName) {
        if (StringUtils.isBlank(sequenceName))
            throw new IllegalArgumentException("Sequence name can not be blank");
        if (!SEQ_PATTERN.matcher(sequenceName).matches())
            throw new IllegalArgumentException(
                    String.format("Invalid sequence name: '%s'. It can contain only alphanumeric characters and underscores", sequenceName));
    }
}
