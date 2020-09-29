/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.navigation.entityinference

import com.haulmont.cuba.core.model.common.User
import io.jmix.ui.WindowInfo
import io.jmix.ui.navigation.EditorTypeExtractor
import io.jmix.ui.screen.FrameOwner
import org.dom4j.tree.BaseElement
import spec.haulmont.cuba.web.navigation.entityinference.testscreens.notype.ExtBaseAbstEditorNT
import spec.haulmont.cuba.web.navigation.entityinference.testscreens.notype.ScreenExtAbstEditorNT
import spec.haulmont.cuba.web.navigation.entityinference.testscreens.withtype.ExtBaseAbstEditor
import spec.haulmont.cuba.web.navigation.entityinference.testscreens.withtype.ScreenExtAbstEditor
import spock.lang.Specification

class EditorEntityTypeInferenceTest extends Specification {

    def 'Screen extends AbstractEditor<User>'() {
        def windowInfo = getWindowInfoFor(ScreenExtAbstEditor)

        when: 'entity type is specified in AbstractEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    def 'Screen extends BaseAbstEditor extends AbstractEditor<User>'() {
        def windowInfo = getWindowInfoFor(ExtBaseAbstEditor)

        when: 'entity type is specified in parent in AbstractEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    // No type declared

    def 'Screen extends AbstractEditor'() {
        def windowInfo = getWindowInfoFor(ScreenExtAbstEditorNT)

        when: 'entity type is not specified in AbstractEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type cannot be extracted'
        entityClass == null
    }

    def 'Screen extends BaseAbstEditor extends AbstractEditor'() {
        def windowInfo = getWindowInfoFor(ExtBaseAbstEditorNT)

        when: 'entity type is not specified in parent in AbstractEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type cannot be extracted'
        entityClass == null
    }

    // Util

    protected getWindowInfoFor(Class<? extends FrameOwner> controllerClass) {
        def wi = new WindowInfo('paramParentEditor', null, new BaseElement('window'))

        wi = Spy(wi)
        wi.getControllerClass() >> controllerClass

        wi
    }
}
