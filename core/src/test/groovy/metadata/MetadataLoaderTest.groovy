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

import io.jmix.core.CoreConfiguration
import io.jmix.core.impl.MetadataLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.base.entity.BaseUuidEntity

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration])
class MetadataLoaderTest extends Specification {

    @Autowired
    MetadataLoader metadataLoader

    def "loads metadata from core and add-on"() {

        given:

        def session = metadataLoader.getSession()

        expect:

        session.findClass(BaseUuidEntity)
        session.findClass(TestAddon1Entity)
    }
}
