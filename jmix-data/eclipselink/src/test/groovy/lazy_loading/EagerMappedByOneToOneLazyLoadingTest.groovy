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

package lazy_loading

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import test_support.DataSpec
import test_support.entity.lazyloading.eager_mapped_by.EagerMappedByPassport
import test_support.entity.lazyloading.eager_mapped_by.EagerMappedByPerson
import test_support.entity.lazyloading.eager_mapped_by.EagerMappedByVisit

/**
 * Covers "Unable to access value holder for property" error for {@code @OneToOne(mappedBy = ...)}
 * attributes declared without {@code fetch = FetchType.LAZY}.
 * <p>
 * Such attributes are EAGER at enhancing time, so no {@code _persistence_<name>_vh} field is woven
 * for them, and lazy loading post-processing must skip them instead of failing.
 */
@IgnoreIf({ Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING")) })
class EagerMappedByOneToOneLazyLoadingTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    EntityStates entityStates

    EagerMappedByPassport passport
    EagerMappedByPerson person
    EagerMappedByVisit visit

    void setup() {
        passport = dataManager.create(EagerMappedByPassport)
        passport.number = '123'

        person = dataManager.create(EagerMappedByPerson)
        person.name = 'Bob'
        person.passport = passport

        visit = dataManager.create(EagerMappedByVisit)
        visit.passport = passport

        dataManager.save(passport, person, visit)
    }

    void cleanup() {
        jdbc.update('delete from TEST_EAGER_MAPPED_BY_VISIT')
        jdbc.update('delete from TEST_EAGER_MAPPED_BY_PERSON')
        jdbc.update('delete from TEST_EAGER_MAPPED_BY_PASSPORT')
    }

    def "direct load of entity with non-lazy mappedBy one-to-one attribute"() {
        when:
        EagerMappedByPassport loadedPassport = dataManager.load(EagerMappedByPassport).id(passport.id).one()

        then:
        loadedPassport.number == '123'
        loadedPassport.person.id == person.id
    }

    def "lazy loading a reference to entity with non-lazy mappedBy one-to-one, owner is of the same class as the attribute"() {
        // the value holder post-processing traversal takes the 'replaceToExistingReferences' branch
        when:
        EagerMappedByPerson loadedPerson = dataManager.load(EagerMappedByPerson).id(person.id).one()

        then:
        !entityStates.isLoaded(loadedPerson, 'passport')

        when:
        EagerMappedByPassport loadedPassport = loadedPerson.passport

        then:
        loadedPassport.number == '123'
        loadedPassport.person.id == person.id
    }

    def "lazy loading a reference to entity with non-lazy mappedBy one-to-one, owner is of an unrelated class"() {
        // the value holder post-processing traversal takes the 'replaceLoadOptions' branch
        when:
        EagerMappedByVisit loadedVisit = dataManager.load(EagerMappedByVisit).id(visit.id).one()

        then:
        !entityStates.isLoaded(loadedVisit, 'passport')

        when:
        EagerMappedByPassport loadedPassport = loadedVisit.passport

        then:
        loadedPassport.number == '123'
        loadedPassport.person.id == person.id
    }
}
