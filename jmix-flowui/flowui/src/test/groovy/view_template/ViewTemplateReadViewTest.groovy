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

package view_template

import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.router.RouteParameters
import io.jmix.core.Metadata
import io.jmix.flowui.Views
import io.jmix.flowui.action.AdjustWhenViewReadOnly
import io.jmix.flowui.action.list.ReadAction
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.view.DefaultMainViewParent
import io.jmix.flowui.view.StandardDetailView
import io.jmix.flowui.view.StandardReadView
import io.jmix.flowui.view.ViewControllerUtils
import io.jmix.flowui.view.ViewRegistry
import io.jmix.flowui.view.template.impl.TemplateReadView
import io.jmix.flowui.view.template.impl.ViewTemplateDefinitions
import io.jmix.flowui.view.template.impl.ViewTemplateDescriptorRegistry
import io.jmix.flowui.view.template.impl.ViewTemplateType
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.util.ReflectionTestUtils
import test_support.entity.viewtemplate.ViewTemplateReadEntity
import test_support.entity.viewtemplate.ViewTemplateTestEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ViewTemplateReadViewTest extends FlowuiTestSpecification {

    protected static final String READ_VIEW_ID = "test_ViewTemplateReadEntity.read"
    protected static final String READ_VIEW_ROUTE = "test-viewtemplatereadentity-read/:id/read"
    protected static final String CONFIGURED_READ_VIEW_ID = "test_ViewTemplateEntity.show"
    protected static final String CONFIGURED_READ_VIEW_ROUTE = "templates/view-template/show/:id/read"
    protected static final String BINDINGS_READ_VIEW_ID = "test_ViewTemplateBindingsEntity.read"
    protected static final String MASTER_READ_VIEW_ID = "test_ViewTemplateMasterEntity.read"
    protected static final String LOOKUP_READ_VIEW_ID = "test_ViewTemplateLookupEntity.show"
    protected static final String CUSTOM_READ_ENTITY_DC_ID = "customerDc"

    @Autowired
    Metadata metadata

    @Autowired
    ViewRegistry viewRegistry

    @Autowired
    ViewTemplateDescriptorRegistry descriptorRegistry

    @Autowired
    ViewTemplateDefinitions viewTemplateDefinitions

    @Autowired
    Views views

    def "read view template is registered under the default read view id"() {
        when:
        def viewInfo = viewRegistry.getViewInfo(READ_VIEW_ID)

        then: "its controller is a generated subclass of the read template view"
        TemplateReadView.isAssignableFrom(viewInfo.controllerClass)
        StandardReadView.isAssignableFrom(viewInfo.controllerClass)
    }

    def "generated read view gets the default route with the read suffix"() {
        given:
        def controllerClass = viewRegistry.getViewInfo(READ_VIEW_ID).controllerClass

        when:
        def route = controllerClass.getAnnotation(Route)

        then: "the whole route, not only its suffix, is derived from the view id"
        route != null
        route.value() == READ_VIEW_ROUTE
        route.layout() == DefaultMainViewParent

        and: "it is registered, so a url can be built for an entity id"
        urlOf(controllerClass, "42") == "test-viewtemplatereadentity-read/42/read"
    }

    def "configured read viewRoute becomes the route of the registered view"() {
        given:
        def controllerClass = viewRegistry.getViewInfo(CONFIGURED_READ_VIEW_ID).controllerClass

        expect: "the annotation route is used as the base and the read suffix is appended to it"
        controllerClass.getAnnotation(Route).value() == CONFIGURED_READ_VIEW_ROUTE
        urlOf(controllerClass, "42") == "templates/view-template/show/42/read"
    }

    def "generated read view is the entity's primary read view"() {
        given: "an entity whose generated read view id does not follow the convention"
        def metaClass = metadata.getClass(ViewTemplateTestEntity)

        expect: "resolution can only find it through the primary read view mapping"
        viewRegistry.getReadViewInfo(metaClass).id == CONFIGURED_READ_VIEW_ID
    }

    def "read view title defaults to the entity name"() {
        expect:
        DocumentHelper.parseText(descriptorOf(READ_VIEW_ID)).rootElement.attributeValue("title") ==
                "test_ViewTemplateReadEntity"
    }

    def "configured read view title wins over the default"() {
        expect:
        DocumentHelper.parseText(descriptorOf(CONFIGURED_READ_VIEW_ID)).rootElement
                .attributeValue("title") == "Template entity card"
    }

    def "configured read viewRoute gets the read suffix"() {
        expect:
        resolveRoutePath("test_X.read", ['viewRoute': 'orders']) == "orders/:id/read"
    }

    def "configured read viewRoute must not already carry the route parameter"() {
        when: "the route already ends with the parameter or with the whole read suffix"
        resolveRoutePath("test_X.read", ['viewRoute': route])

        then: "it is rejected instead of duplicating the parameter"
        def e = thrown(IllegalArgumentException)
        e.message.contains("Read viewRoute must not end with")

        where:
        route << ['orders/:id', 'orders/:id/read']
    }

    def "generated descriptor shows the entity and cannot save it"() {
        when:
        def descriptor = descriptorOf(READ_VIEW_ID)
        def root = DocumentHelper.parseText(descriptor).rootElement
        def instance = root.element("data").element("instance")

        then: "the shown entity is loaded into the default container through its own loader"
        instance.attributeValue("id") == "entityDc"
        instance.element("loader") != null
        root.element("facets").element("dataLoadCoordinator").attributeValue("auto") == "true"

        and: "the fetch plan carries the properties the view shows, so none of them stays unfetched"
        instance.element("fetchPlan").elements("property")*.attributeValue("name").sort() ==
                ["active", "name"]

        and: "an entity without collections keeps a bare form, with no tab sheet around it"
        root.element("layout").element("tabSheet") == null

        and: "the fields are rendered inside the form bound to that container"
        def form = root.element("layout").element("formLayout")
        form.attributeValue("id") == "form"
        form.attributeValue("dataContainer") == "entityDc"
        form.elements()*.attributeValue("id") == ["nameField", "activeField"]

        and: "the view's own actions close it and nothing else"
        def actions = root.element("actions").elements()
        actions*.attributeValue("type") == ["view_close"]
        descriptor.contains('<hbox id="readActions">')

        and: "read-only is the class's job, so nothing in the layout declares it"
        // A substring check would be wrong here: the items container of an eager reference legitimately
        // carries a readOnly loader under <data>. The invariant is about the view's own data element and
        // its components.
        root.element("data").attributeValue("readOnly") == null
        elementsWithReadOnly(root.element("layout")).isEmpty()
    }

    def "generated read view is created with the shown entity container bound"() {
        when:
        def view = views.create(READ_VIEW_ID)

        then: "the generated controller is instantiated from the rendered descriptor"
        view instanceof TemplateReadView
        view.id.orElseThrow() == READ_VIEW_ID
        view.pageTitle == "test_ViewTemplateReadEntity"

        and: "its shown entity container is the one the template declares"
        def viewData = ViewControllerUtils.getViewData(view)
        entityContainerOf(view).is(viewData.getContainer("entityDc"))
    }

    def "generated read view uses the container id configured on the annotation"() {
        when:
        def view = views.create(BINDINGS_READ_VIEW_ID)

        then: "the generated controller returns the configured container, not the default one"
        def viewData = ViewControllerUtils.getViewData(view)
        entityContainerOf(view).is(viewData.getContainer(CUSTOM_READ_ENTITY_DC_ID))

        and: "so the default container id is not even declared by that template"
        !descriptorOf(BINDINGS_READ_VIEW_ID).contains('<instance id="entityDc"')
    }

    def "read view template renders composition collections with the read action only"() {
        when:
        def descriptor = descriptorOf(MASTER_READ_VIEW_ID)
        def root = DocumentHelper.parseText(descriptor).rootElement
        def instance = root.element("data").element("instance")

        then: "the composition is fetched and loaded into a nested container"
        instance.element("fetchPlan").elements("property")*.attributeValue("name").contains("lines")
        instance.elements("collection")*.attributeValue("id") == ["linesDc"]
        instance.element("collection").attributeValue("property") == "lines"

        and: "the form moves into a tab sheet that gets one more tab per composition"
        def tabSheet = root.element("layout").element("tabSheet")
        tabSheet != null
        def tabs = tabSheet.elements("tab")
        tabs*.attributeValue("id") == ["generalTab", "linesTab"]
        tabs[0].element("formLayout").attributeValue("dataContainer") == "entityDc"

        and: "the grid is bound to the nested container and shows the line's own properties"
        def content = tabs[1].element("vbox")
        def grid = content.element("dataGrid")
        grid.attributeValue("id") == "linesDataGrid"
        grid.attributeValue("dataContainer") == "linesDc"
        grid.element("columns").elements("column")*.attributeValue("property") == ["description", "quantity"]

        and: "reading the line in a dialog is the only action it carries"
        def actions = grid.element("actions").elements()
        actions*.attributeValue("type") == ["list_read"]
        actions[0].element("properties").elements("property").collect {
            [it.attributeValue("name"), it.attributeValue("value")]
        } == [["openMode", "DIALOG"]]

        and: "and the only button over the grid"
        content.element("hbox").elements("button")*.attributeValue("action") == ["linesDataGrid.readAction"]

        and: "an association collection is not rendered at all"
        !descriptor.contains("relatedCustomers")
    }

    def "collection grid of a created read view carries a usable read action"() {
        when: "the descriptor with a tab sheet and a collection grid is loaded"
        def view = views.create(MASTER_READ_VIEW_ID)

        then: "the grid is bound to the nested container"
        def grid = UiComponentUtils.getComponent(view, "linesDataGrid")
        grid.dataProvider != null

        and: "its action is a read action, which the read-only state does not touch"
        def action = grid.getAction("readAction")
        action instanceof ReadAction
        !(action instanceof AdjustWhenViewReadOnly)
    }

    def "read view template renders a @LookupField reference like the detail template does"() {
        when:
        def descriptor = descriptorOf(LOOKUP_READ_VIEW_ID)

        then: "a reference whose entity asks for a dropdown becomes an entityComboBox"
        descriptor.contains('id="productField"')
        descriptor.contains('<entityComboBox')
        descriptor.contains('byInstanceName="true"')
        !descriptor.contains('<entityPicker')

        and: "an eager dropdown reference also gets its items container, and the field is bound to it"
        descriptor.contains('<collection id="countryItemsDc"')
        descriptor.contains('select e from test_LfCountry e')
        descriptor.contains('itemsContainer="countryItemsDc"')
    }

    protected String urlOf(Class controllerClass, String entityId) {
        RouteConfiguration.forSessionScope().getUrl(controllerClass,
                new RouteParameters(StandardDetailView.DEFAULT_ROUTE_PARAM, entityId))
    }

    /**
     * Returns the container the read view shows the entity in. {@code getEntityContainer()} is the
     * extension point the generated controller overrides, so going through it is what proves the
     * configured container id reached the generated class.
     */
    protected entityContainerOf(view) {
        ReflectionTestUtils.invokeMethod(view, "getEntityContainer")
    }

    /**
     * Calls the protected route resolution of the definitions bean. It is an extension point rather than
     * public API, and a fixture with an invalid route would fail the whole application context, so the
     * rejection is exercised directly here.
     */
    protected String resolveRoutePath(String viewId, Map<String, Object> attributes) {
        ReflectionTestUtils.invokeMethod(viewTemplateDefinitions, "resolveRoutePath",
                viewId, ViewTemplateType.READ, attributes)
    }

    protected List<Element> elementsWithReadOnly(Element element) {
        def result = []
        if (element.attributeValue("readOnly") != null) {
            result << element
        }
        element.elements().each { result.addAll(elementsWithReadOnly(it)) }
        return result
    }

    protected String descriptorOf(String viewId) {
        descriptorRegistry.getDescriptor(viewRegistry.getViewInfo(viewId).templatePath.orElseThrow())
                .orElseThrow()
    }
}
