/*
 * Copyright 2022 Haulmont.
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

package fetch_plans

import io.jmix.core.CoreConfiguration
import io.jmix.core.FetchPlanBuilder
import io.jmix.core.FetchPlanRepository
import io.jmix.core.Metadata
import io.jmix.core.common.util.Dom4j
import io.jmix.core.impl.FetchPlanLoader
import org.dom4j.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class FetchPlanLoaderTest extends Specification {

    @Autowired
    Metadata metadata
    @Autowired
    FetchPlanLoader fetchPlanLoader
    @Autowired
    FetchPlanRepository fetchPlanRepository


    def "inline fetchPlan has only explicitly specified properties"() {

        def xml = '''
            <fetchPlan extends="_instance_name">
                <property name="number"/>
                <property name="customer">
                    <property name="name"/>
                </property>
            </fetchPlan>
            '''
        Document document = Dom4j.readDocument(xml)
        def element = document.getRootElement()

        when:

        FetchPlanLoader.FetchPlanInfo fpInfo = fetchPlanLoader.getFetchPlanInfo(element, metadata.getClass(Order))
        FetchPlanBuilder builder = fetchPlanLoader.getFetchPlanBuilder(fpInfo, a ->
                fetchPlanRepository.getFetchPlan(fpInfo.getMetaClass(), a))
        fetchPlanLoader.loadFetchPlanProperties(element, builder, fpInfo.isSystemProperties(), (metaClass, fpName) ->
                fetchPlanRepository.getFetchPlan(metaClass, fpName))
        def fetchPlan = builder.build()

        then:
        fetchPlan.containsProperty('number')
        !fetchPlan.containsProperty('date')
        fetchPlan.containsProperty('customer')
        // customer FP has only declared properties
        fetchPlan.getProperty('customer').fetchPlan.containsProperty('name')
        !fetchPlan.getProperty('customer').fetchPlan.containsProperty('status')
    }

    def "default fetchPlan for property is _base"() {

        def xml = '''
            <fetchPlan extends="_instance_name">
                <property name="number"/>
                <property name="customer"/>
            </fetchPlan>
            '''
        Document document = Dom4j.readDocument(xml)
        def element = document.getRootElement()

        when:

        FetchPlanLoader.FetchPlanInfo fpInfo = fetchPlanLoader.getFetchPlanInfo(element, metadata.getClass(Order))
        FetchPlanBuilder builder = fetchPlanLoader.getFetchPlanBuilder(fpInfo, a ->
                fetchPlanRepository.getFetchPlan(fpInfo.getMetaClass(), a))
        fetchPlanLoader.loadFetchPlanProperties(element, builder, fpInfo.isSystemProperties(), (metaClass, fpName) ->
                fetchPlanRepository.getFetchPlan(metaClass, fpName))
        def fetchPlan = builder.build()

        then:
        fetchPlan.containsProperty('number')
        !fetchPlan.containsProperty('date')
        fetchPlan.containsProperty('customer')
        // customer FP has all properties
        fetchPlan.getProperty('customer').fetchPlan.containsProperty('name')
        fetchPlan.getProperty('customer').fetchPlan.containsProperty('status')
    }
}
