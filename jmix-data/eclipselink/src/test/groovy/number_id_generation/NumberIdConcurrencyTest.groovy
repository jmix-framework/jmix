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

package number_id_generation

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.data.DataConfigPropertiesAccess
import io.jmix.data.DataProperties
import io.jmix.data.persistence.SequenceSupport
import io.jmix.data.impl.NumberIdCache
import io.jmix.data.impl.NumberIdWorker
import io.jmix.data.persistence.DbmsSpecifics
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.number_id_generation.NumberIdSingleTableRoot

import javax.sql.DataSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NumberIdConcurrencyTest extends DataSpec {

    @Autowired
    private Metadata metadata
    @Autowired
    private DataManager dataManager
    @Autowired
    private DbmsSpecifics dbmsSpecifics
    @Autowired
    private NumberIdWorker numberIdWorker
    @Autowired
    private NumberIdCache numberIdCache
    @Autowired
    private DataProperties dataProperties
    @Autowired
    private DataSource dataSource

    private SequenceSupport sequenceSupport

    private Logger log = LoggerFactory.getLogger(NumberIdConcurrencyTest)

    void setup() {
        sequenceSupport = dbmsSpecifics.getSequenceSupport()

        cleanupSequences()

    }

    void cleanup() {
        cleanupSequences()

        def template = new JdbcTemplate(dataSource)
        template.update('delete from TEST_NUMBER_ID_SINGLE_TABLE_ROOT')
    }

    protected void cleanupSequences() {
        numberIdWorker.reset()
        numberIdCache.reset()

        if (sequenceExists()) {
            def sql = sequenceSupport.deleteSequenceSql(getSequenceName('test$NumberIdSingleTableRoot'))
            def template = new JdbcTemplate(dataSource)
            template.update(sql)
        }
    }

    def "generating ids with increment 1"() {

        DataConfigPropertiesAccess.setNumberIdCacheSize(dataProperties, 1)

        when:

        generateSomeEntities(100)

        then:

        countEntities() == 100

        getNextSequenceValue() == 100

        cleanup:

        DataConfigPropertiesAccess.setNumberIdCacheSize(dataProperties, 100)
    }

    def "generating ids with increment 20"() {

        DataConfigPropertiesAccess.setNumberIdCacheSize(dataProperties, 20)

        when:

        generateSomeEntities(500)

        then:

        countEntities() == 500

        cleanup:

        DataConfigPropertiesAccess.setNumberIdCacheSize(dataProperties, 100)
    }

    def "generate with zero size cache"() {
        DataConfigPropertiesAccess.setNumberIdCacheSize(dataProperties, 0)

        when:

        NumberIdSingleTableRoot foo = metadata.create(NumberIdSingleTableRoot)
        foo.setName('item-1')
        foo = dataManager.save(foo)

        then:

        foo.id == getCurrentSequenceValue()

        when:

        foo = metadata.create(NumberIdSingleTableRoot)
        foo.setName('item-2')
        foo = dataManager.save(foo)

        then:

        foo.id == getCurrentSequenceValue()

        cleanup:

        DataConfigPropertiesAccess.setNumberIdCacheSize(dataProperties, 100)
    }

    private void generateSomeEntities(int count) {
        long start = System.currentTimeMillis()
        ExecutorService executorService = Executors.newFixedThreadPool(10)
        List<String> failed = []
        for (i in 1..count) {
            String idx = "$i"
            executorService.submit({
                try {
                    NumberIdSingleTableRoot foo = metadata.create(NumberIdSingleTableRoot)
                    foo.setName('item-' + idx.padLeft(4, '0'))
                    dataManager.save(foo)
                } catch (Exception e) {
                    log.error('Error creating entity', e)
                }
            })
        }
        executorService.shutdown()
        boolean terminated
        try {
            terminated = executorService.awaitTermination(10, TimeUnit.SECONDS)
        } catch (InterruptedException e) {
            throw new RuntimeException(e)
        }
        if (!terminated)
            log.warn("Termination timed out")
        log.info("Completed in ${System.currentTimeMillis() - start}ms, Failed: $failed")
    }

    private String getSequenceName(String entityName) {
        return "seq_id_" + entityName.replace('$', '_')
    }

    private boolean sequenceExists() {
        def sequenceExistsSql = sequenceSupport.sequenceExistsSql(getSequenceName('test$NumberIdSingleTableRoot'))
        def template = new JdbcTemplate(dataSource)
        def rows = template.queryForList(sequenceExistsSql)
        return !rows.isEmpty()
    }


    private long getNextSequenceValue() {
        def sql = sequenceSupport.getNextValueSql(getSequenceName('test$NumberIdSingleTableRoot'))
        def template = new JdbcTemplate(dataSource)
        return template.queryForObject(sql, Long.class)
    }

    private long getCurrentSequenceValue() {
        def sql = sequenceSupport.getCurrentValueSql(getSequenceName('test$NumberIdSingleTableRoot'))
        def template = new JdbcTemplate(dataSource)
        return template.queryForObject(sql, Long.class)
    }

    private long countEntities() {
        def template = new JdbcTemplate(dataSource)
        return template.queryForObject("select count(*) from TEST_NUMBER_ID_SINGLE_TABLE_ROOT", Long.class)
    }
}
