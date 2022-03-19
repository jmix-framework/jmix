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

package spec.haulmont.cuba.core.metadata

import com.haulmont.bali.db.ListArrayHandler
import com.haulmont.bali.db.QueryRunner
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.model.number_id.NumberIdSeqNameFirst
import com.haulmont.cuba.core.model.number_id.NumberIdSeqNameSecond
import io.jmix.data.persistence.SequenceSupport
import io.jmix.data.persistence.DbmsSpecifics
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.sql.DataSource

class NumberIdGenerationTest extends CoreTestSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    DataSource dataSource
    @Autowired
    DbmsSpecifics dbmsSpecifics

    SequenceSupport sequenceSupport

    void setup() {
        sequenceSupport = dbmsSpecifics.getSequenceSupport()
    }

    def "sequence name annotation"() {

        when:

        def first = metadata.create(NumberIdSeqNameFirst)
        def second = metadata.create(NumberIdSeqNameSecond)

        then:

        !sequenceExistsByEntityName(metadata.getClassNN(NumberIdSeqNameFirst).getName())
        !sequenceExistsByEntityName(metadata.getClassNN(NumberIdSeqNameSecond).getName())
        sequenceExistsByName('seq_number_id_name')
        first.id + 1  == second.id
        getCurrentSequenceValue('seq_number_id_name') == second.id

        when:

        first = metadata.create(NumberIdSeqNameFirst)

        then:

        !sequenceExistsByEntityName(metadata.getClassNN(NumberIdSeqNameFirst).getName())
        !sequenceExistsByEntityName(metadata.getClassNN(NumberIdSeqNameSecond).getName())
        sequenceExistsByName('seq_number_id_name')
        getCurrentSequenceValue('seq_number_id_name') == first.id

        when:

        second = metadata.create(NumberIdSeqNameSecond)

        then:

        !sequenceExistsByEntityName(metadata.getClassNN(NumberIdSeqNameFirst).getName())
        !sequenceExistsByEntityName(metadata.getClassNN(NumberIdSeqNameSecond).getName())
        sequenceExistsByName('seq_number_id_name')
        getCurrentSequenceValue('seq_number_id_name') == second.id
        first.id + 1  == second.id
    }

    private boolean sequenceExistsByEntityName(String entityName) {
        return sequenceExistsByName(getSequenceName(entityName))
    }

    private boolean sequenceExistsByName(String sequenceName) {
        def sequenceExistsSql = sequenceSupport.sequenceExistsSql(sequenceName)
        def runner = new QueryRunner(dataSource)
        List<Object[]> seqRows = runner.query(sequenceExistsSql, new ListArrayHandler())
        return !seqRows.isEmpty()
    }

    protected String getSequenceName(String entityName) {
        return "seq_id_" + entityName.replace('$', '_');
    }

    private long getCurrentSequenceValue(String sequenceName) {
        def sql = "select NEXT_VALUE from INFORMATION_SCHEMA.SYSTEM_SEQUENCES where SEQUENCE_NAME = '" + sequenceName.toUpperCase() + "'"
        def runner = new QueryRunner(dataSource)
        List<Object[]> seqRows = runner.query(sql, new ListArrayHandler())
        return (seqRows[0][0] as long) - 1
    }
}
