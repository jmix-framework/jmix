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

package liquibase

import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixModules
import io.jmix.core.Stores
import io.jmix.data.DataConfiguration
import io.jmix.data.impl.liquibase.LiquibaseChangeLogProcessor
import io.jmix.eclipselink.EclipselinkConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.DataTestConfiguration
import test_support_modules.addon.TestAddonConfiguration
import test_support_modules.app.TestAppConfiguration

@ContextConfiguration(classes = [
        CoreConfiguration, DataConfiguration, EclipselinkConfiguration, DataTestConfiguration, TestAddonConfiguration, TestAppConfiguration])
class LiquibaseChangeLogProcessorTest extends Specification {

    @Autowired
    Environment environment
    @Autowired
    JmixModules jmixModules

    def "master file creation"() {
        def processor = new LiquibaseChangeLogProcessor(environment, jmixModules)

        when:
        def masterFileContent = processor.createMasterChangeLog(Stores.MAIN)

        then:
        def databaseChangeLog = new XmlSlurper().parseText(masterFileContent)
        databaseChangeLog.include[0].@file == '/io/jmix/data/liquibase/changelog.xml'
        databaseChangeLog.include[1].@file == '/test_support_modules/addon/liquibase/changelog.xml'
        databaseChangeLog.include[2].@file == '/test_support_modules/app/liquibase/changelog.xml'
    }

    def "master file creation for additional data store"() {
        def processor = new LiquibaseChangeLogProcessor(environment, jmixModules)

        when:
        def masterFileContent = processor.createMasterChangeLog('db1')

        then:
        def databaseChangeLog = new XmlSlurper().parseText(masterFileContent)
        databaseChangeLog.include[0].@file == '/test_support_modules/addon/liquibase/db1-changelog.xml'
        databaseChangeLog.include[1].@file == '/test_support_modules/app/liquibase/db1-changelog.xml'
    }
}
