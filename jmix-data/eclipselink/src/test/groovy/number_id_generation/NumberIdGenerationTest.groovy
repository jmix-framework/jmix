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

import test_support.entity.number_id_generation.NumberIdJoinedChild
import test_support.entity.number_id_generation.NumberIdJoinedRoot
import test_support.entity.number_id_generation.NumberIdSeqNameFirst
import test_support.entity.number_id_generation.NumberIdSeqNameSecond
import test_support.entity.number_id_generation.NumberIdSingleTableChild
import test_support.entity.number_id_generation.NumberIdSingleTableGrandChild
import test_support.entity.number_id_generation.NumberIdSingleTableRoot
import io.jmix.core.Metadata
import io.jmix.core.Stores
import io.jmix.data.persistence.SequenceSupport
import io.jmix.data.StoreAwareLocator
import io.jmix.data.persistence.DbmsSpecifics
import org.springframework.jdbc.core.JdbcTemplate

import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec

class NumberIdGenerationTest extends DataSpec {
    @Autowired
    private Metadata metadata
    @Autowired
    private DbmsSpecifics dbmsSpecifics
    @Autowired
    private StoreAwareLocator storeAwareLocator;

    private SequenceSupport sequenceSupport


    void setup() {
        sequenceSupport = dbmsSpecifics.getSequenceSupport()
    }

    def "joined inheritance strategy"() {

        when:

        def root1 = metadata.create(NumberIdJoinedRoot)
        def root2 = metadata.create(NumberIdJoinedRoot)

        then:

        sequenceExistsByEntityName(metadata.getClass(NumberIdJoinedRoot).getName())
        root2.id == root1.id + 1

        when: "creating child entities"

        def child1 = metadata.create(NumberIdJoinedChild)
        def child2 = metadata.create(NumberIdJoinedChild)

        then: "the same sequence as for root is used"

        !sequenceExistsByEntityName(metadata.getClass(NumberIdJoinedChild).getName())
        child1.id == root2.id + 1
        child2.id == child1.id + 1

    }

    def "single table inheritance strategy"() {

        when:

        def root1 = metadata.create(NumberIdSingleTableRoot)
        def root2 = metadata.create(NumberIdSingleTableRoot)

        then:

        sequenceExistsByEntityName(metadata.getClass(NumberIdSingleTableRoot).getName())
        root2.id == root1.id + 1

        when: "creating child entities"

        def child1 = metadata.create(NumberIdSingleTableChild)
        def child2 = metadata.create(NumberIdSingleTableChild)

        then: "the same sequence as for root is used"

        !sequenceExistsByEntityName(metadata.getClass(NumberIdSingleTableChild).getName())
        child1.id == root2.id + 1
        child2.id == child1.id + 1

        when: "creating grand children entities"

        def grandChild1 = metadata.create(NumberIdSingleTableGrandChild)
        def grandChild2 = metadata.create(NumberIdSingleTableGrandChild)

        then: "the same sequence as for root is used"

        !sequenceExistsByEntityName(metadata.getClass(NumberIdSingleTableChild).getName())
        !sequenceExistsByEntityName(metadata.getClass(NumberIdSingleTableGrandChild).getName())
        grandChild1.id == child2.id + 1
        grandChild2.id == grandChild1.id + 1
    }

    def "sequence name annotation"() {

        when:

        def first = metadata.create(NumberIdSeqNameFirst)
        def second = metadata.create(NumberIdSeqNameSecond)

        then:

        !sequenceExistsByEntityName(metadata.getClass(NumberIdSeqNameFirst).getName())
        !sequenceExistsByEntityName(metadata.getClass(NumberIdSeqNameSecond).getName())
        sequenceExistsByName('seq_number_id_name')
        first.id + 1 == second.id
        getCurrentSequenceValue('seq_number_id_name') == second.id

        when:

        first = metadata.create(NumberIdSeqNameFirst)

        then:

        !sequenceExistsByEntityName(metadata.getClass(NumberIdSeqNameFirst).getName())
        !sequenceExistsByEntityName(metadata.getClass(NumberIdSeqNameSecond).getName())
        sequenceExistsByName('seq_number_id_name')
        getCurrentSequenceValue('seq_number_id_name') == first.id

        when:

        second = metadata.create(NumberIdSeqNameSecond)

        then:

        !sequenceExistsByEntityName(metadata.getClass(NumberIdSeqNameFirst).getName())
        !sequenceExistsByEntityName(metadata.getClass(NumberIdSeqNameSecond).getName())
        sequenceExistsByName('seq_number_id_name')
        getCurrentSequenceValue('seq_number_id_name') == second.id
        first.id + 1 == second.id
    }

    private boolean sequenceExistsByEntityName(String entityName) {
        return sequenceExistsByName(getSequenceName(entityName))
    }

    private boolean sequenceExistsByName(String sequenceName) {
        def sequenceExistsSql = sequenceSupport.sequenceExistsSql(sequenceName)
        def template = new JdbcTemplate(storeAwareLocator.getDataSource(Stores.MAIN))
        def rows = template.queryForList(sequenceExistsSql)
        return !rows.isEmpty()
    }

    protected String getSequenceName(String entityName) {
        return "seq_id_" + entityName.replace('$', '_');
    }

    private long getCurrentSequenceValue(String sequenceName) {
        def sql = "select NEXT_VALUE from INFORMATION_SCHEMA.SYSTEM_SEQUENCES where SEQUENCE_NAME = '" + sequenceName.toUpperCase() + "'"
        def template = new JdbcTemplate(storeAwareLocator.getDataSource(Stores.MAIN))
        def rows = template.queryForList(sql)
        return (rows[0]['NEXT_VALUE'] as long) - 1
    }
}
