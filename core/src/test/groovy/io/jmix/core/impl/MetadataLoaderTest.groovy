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

package io.jmix.core.impl

import com.sample.addon1.TestAddon1Configuration
import com.sample.addon1.entity.TestAddon1Entity
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.entity.BaseUuidEntity
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration])
class MetadataLoaderTest extends Specification {

    @Inject
    MetadataLoader metadataLoader

    def "loads metadata from core and add-on"() {
        expect:

        def session = metadataLoader.getSession()

        session.getClass(BaseUuidEntity) != null
        session.getClass(TestAddon1Entity) != null

        metadataLoader.getRootPackages() == ['io.jmix.core', 'com.sample.addon1']
    }
}
