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

package app_properties

import com.haulmont.cuba.core.sys.AppProperties
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class AppPropertiesTest extends CoreTestSpecification {

    @Autowired
    AppProperties appProperties

    def "properties can be changed at runtime"() {
        when:

        appProperties.setProperty('prop3', 'changed_prop3')
        appProperties.setProperty('prop4', 'changed_prop4')

        then:

        appProperties.getProperty('prop3') == 'changed_prop3'
        appProperties.getProperty('prop4') == 'changed_prop4'

        when:

        appProperties.setProperty('prop3', null)
        appProperties.setProperty('prop4', null)

        then:

        appProperties.getProperty('prop3') == 'app_prop3'
        appProperties.getProperty('prop4') == null
    }
}
