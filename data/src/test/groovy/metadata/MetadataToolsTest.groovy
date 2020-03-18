/*
 * Copyright 2019 Haulmont.
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

package metadata

import io.jmix.core.MetadataTools
import test_support.DataSpec
import test_support.entity.petclinic.Owner
import test_support.entity.petclinic.Pet

import javax.inject.Inject

class MetadataToolsTest extends DataSpec {

    @Inject
    private MetadataTools metadataTools

    def "deepCopy handles entities with same ids correctly #73"() {
        def id = new UUID(0, 1)
        def owner = new Owner(id: id, name: 'Joe')
        def pet = new Pet(id: id, name: 'Rex', owner: owner)

        when:
        def petCopy = metadataTools.deepCopy(pet)

        then:
        petCopy.owner == owner
        !petCopy.owner.is(owner)
    }
}
