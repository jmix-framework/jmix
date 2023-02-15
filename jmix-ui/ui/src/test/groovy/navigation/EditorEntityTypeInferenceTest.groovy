/*
 * Copyright (c) 2020 Haulmont.
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

package navigation

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.WindowInfo
import io.jmix.ui.navigation.EditorTypeExtractor
import io.jmix.ui.screen.FrameOwner
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import navigation.screen.no_type.by_class.ExtBaseStdEditorNT
import navigation.screen.no_type.by_class.ScreenExtStdEditorNT
import navigation.screen.no_type.by_interface.ExtBaseEditorScreenNT
import navigation.screen.no_type.by_interface.ScreenImplEditorScreenNT
import navigation.screen.with_type.by_class.ExtBaseStdEditor
import navigation.screen.with_type.by_class.L3StdEditor
import navigation.screen.with_type.by_class.ScreenExtStdEditor
import navigation.screen.with_type.by_interface.ExtBaseEditorScreen
import navigation.screen.with_type.by_interface.ScreenImplEditorScreen
import org.dom4j.tree.BaseElement
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sec.User

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class EditorEntityTypeInferenceTest extends ScreenSpecification {

    def 'Screen implements EditorScreen<User>'() {
        def windowInfo = getWindowInfoFor(ScreenImplEditorScreen)

        when: 'entity type is specified in EditorScreen generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    def 'Screen extends StandardEditor<User>'() {
        def windowInfo = getWindowInfoFor(ScreenExtStdEditor)

        when: 'entity type is specified in StandardEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    def 'Screen extends BaseEditorScreen implements EditorScreen<User>'() {
        def windowInfo = getWindowInfoFor(ExtBaseEditorScreen)

        when: 'entity type is specified in parent in EditorScreen generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    def 'Screen extends BaseStdEditor extends StandardEditor<User>'() {
        def windowInfo = getWindowInfoFor(ExtBaseStdEditor)

        when: 'entity type is specified in parent in StandardEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    def 'Screen extends AbstractL2Editor<User> extends StandardEditor<T>'() {
        def windowInfo = getWindowInfoFor(L3StdEditor)

        when: 'entity type is specified in AbstractL2Editor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type can be extracted'
        User.isAssignableFrom(entityClass)
    }

    // No type declared

    def 'Screen implements EditorScreen'() {
        def windowInfo = getWindowInfoFor(ScreenImplEditorScreenNT)

        when: 'entity type is not specified in EditorScreen generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type cannot be be extracted'
        entityClass == null
    }

    def 'Screen extends StandardEditor'() {
        def windowInfo = getWindowInfoFor(ScreenExtStdEditorNT)

        when: 'entity type is not specified in StandardEditor generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type cannot be extracted'
        entityClass == null
    }

    def 'Screen extends BaseEditorScreen implements EditorScreen'() {
        def windowInfo = getWindowInfoFor(ExtBaseEditorScreenNT)

        when: 'entity type is not specified in parent in EditorScreen generic'
        def entityClass = EditorTypeExtractor.extractEntityClass(windowInfo)

        then: 'type cannot be extracted'
        entityClass == null
    }

    def 'Screen extends BaseStdEditor extends StandardEditor'() {
        def windowInfo = getWindowInfoFor(ExtBaseStdEditorNT)

        when: 'entity type is not specified in parent in StandardEditor generic'
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
