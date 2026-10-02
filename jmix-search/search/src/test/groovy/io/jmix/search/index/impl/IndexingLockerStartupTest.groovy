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

package io.jmix.search.index.impl

import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification

/**
 * What the locker is allowed to do while the beans are being created.
 *
 * <p>Asking for the indexed entities builds the index definitions, and building them reads the database when the
 * Dynamic Attributes module is present: the attributes of an entity are rows in {@code DYNAT_CATEGORY}. A bean
 * constructor runs before Liquibase has created the schema, so an application with that module could not start
 * on a database that does not have those tables yet.
 */
class IndexingLockerStartupTest extends Specification {

    def "the locker asks the index configurations nothing while it is being constructed"() {
        given:
        def indexConfigurationManager = Mock(IndexConfigurationManager)

        when:
        new IndexingLocker(indexConfigurationManager)

        then: """nothing at all: the maps of locks fill themselves on first use, and a prefilled lock would not
                 even be found - a lock is keyed by the pair of an entity and a tenant"""
        0 * indexConfigurationManager._
    }
}
