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

package data_binding_text_component

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.markdown.Markdown
import io.jmix.core.AccessManager
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.flowui.accesscontext.UiEntityAttributeContext
import io.jmix.flowui.data.binding.TextComponentReadonlyDataBinding
import io.jmix.flowui.data.binding.impl.TextComponentReadonlyDataBindingImpl
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.Zoo
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class TextComponentReadonlyDataBindingConsumerTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools
    @Autowired
    DataComponents dataComponents
    @Autowired
    TextComponentReadonlyDataBinding textComponentReadonlyDataBinding

    def "Existing HasText binding keeps static text until an item is set"() {
        def container = dataComponents.createInstanceContainer(Zoo)
        def span = new Span("Static text")

        when:
        textComponentReadonlyDataBinding.bind(span, container, "name")

        then:
        span.text == "Static text"

        when:
        def zoo = metadata.create(Zoo)
        zoo.name = "Zoo"
        container.item = zoo

        then:
        span.text == "Zoo"
    }

    def "Container binding updates content and can be removed"() {
        def container = dataComponents.createInstanceContainer(Zoo)
        def markdown = new Markdown("Old content")

        when: "Bind an empty container"
        def registration = textComponentReadonlyDataBinding.bind(markdown, container, "name", markdown.&setContent)

        then: "The content is kept until an item is set, as with HasText components"
        markdown.content == "Old content"

        when:
        def zoo = metadata.create(Zoo)
        zoo.name = "## Zoo"
        container.item = zoo

        then:
        markdown.content == "## Zoo"

        when: "Change displayed content directly"
        markdown.content = "Display only"

        then: "The binding does not write back"
        zoo.name == "## Zoo"

        when:
        registration.remove()
        zoo.name = "Updated zoo"

        then:
        markdown.content == "Display only"

        when:
        def otherZoo = metadata.create(Zoo)
        otherZoo.name = "Other zoo"
        container.item = otherZoo

        then:
        markdown.content == "Display only"
    }

    def "Value source binding updates content and can be removed"() {
        def container = dataComponents.createInstanceContainer(Zoo)
        def zoo = metadata.create(Zoo)
        zoo.name = "## Zoo"
        container.item = zoo
        def valueSource = new ContainerValueSource<>(container, "name")
        valueSource.setApplicationContext(applicationContext)
        def markdown = new Markdown()

        when:
        def registration = textComponentReadonlyDataBinding.bind(markdown, valueSource, markdown.&setContent)

        then:
        markdown.content == "## Zoo"

        when:
        valueSource.value = "**Updated zoo**"

        then:
        markdown.content == "**Updated zoo**"

        when:
        valueSource.value = null

        then:
        markdown.content == ""

        when: "The value source changes its current entity"
        def otherZoo = metadata.create(Zoo)
        otherZoo.name = "Other zoo"
        container.item = otherZoo

        then:
        markdown.content == "Other zoo"

        when: "The value source loses its current entity"
        container.item = null

        then:
        markdown.content == ""

        when:
        registration.remove()
        container.item = zoo
        valueSource.value = "Unbound"

        then:
        markdown.content == ""
    }

    def "Value source binding ignores changes in non-current collection items"() {
        def container = dataComponents.createCollectionContainer(Zoo)
        def first = metadata.create(Zoo)
        first.name = "First zoo"
        def second = metadata.create(Zoo)
        second.name = "Second zoo"
        container.items = [first, second]
        container.item = first
        def valueSource = new ContainerValueSource<>(container, "name")
        valueSource.setApplicationContext(applicationContext)
        def markdown = new Markdown()
        textComponentReadonlyDataBinding.bind(markdown, valueSource, markdown.&setContent)

        when:
        second.name = "Updated second zoo"

        then:
        markdown.content == "First zoo"
    }

    def "Binding hides markdown when attribute view permission is denied"() {
        def container = dataComponents.createInstanceContainer(Zoo)
        def accessManager = Mock(AccessManager)
        def binding = new TextComponentReadonlyDataBindingImpl(metadataTools, accessManager)
        def markdown = new Markdown()

        when:
        if (valueSourceBinding) {
            def valueSource = new ContainerValueSource<>(container, "name")
            valueSource.setApplicationContext(applicationContext)
            binding.bind(markdown, valueSource, markdown.&setContent)
        } else {
            binding.bind(markdown, container, "name", markdown.&setContent)
        }

        then:
        1 * accessManager.applyRegisteredConstraints(_ as UiEntityAttributeContext) >> {
            UiEntityAttributeContext context -> context.setViewDenied()
        }
        !markdown.visible

        where:
        valueSourceBinding << [false, true]
    }
}
