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

package component.image

import component.image.view.JmixImageTestView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class JmixImageTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerScreenBasePackages("component.image")
    }

    def "Load JmixImage with dataContainer"() {
        when: "Open screen with Image that is bound with data container"
        def screen = openScreen(JmixImageTestView)

        then:

        !screen.imageByteArray.getSrc().isEmpty()
    }
}
