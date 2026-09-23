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

import io.jmix.core.DataManager
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.ViewControllerUtils
import io.jmix.flowui.view.ReadOnlyAwareView
import io.jmix.flowui.view.template.impl.TemplateReadView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.viewtemplate.ViewTemplateLineEntity
import test_support.entity.viewtemplate.ViewTemplateMasterEntity
import test_support.entity.viewtemplate.ViewTemplateReadEntity
import test_support.spec.FlowuiTestSpecification

/**
 * Behavior of a read view generated from {@code @ReadViewTemplate} once it is actually opened for a
 * stored entity. The descriptor's shape is pinned by {@link ViewTemplateReadViewTest}; this spec is
 * about what the running view does, so every case here needs the database.
 */
@SpringBootTest
class ViewTemplateReadViewRuntimeTest extends FlowuiTestSpecification {

    protected static final String READ_VIEW_ID = "test_ViewTemplateReadEntity.read"
    protected static final String READ_LIST_VIEW_ID = "test_ViewTemplateReadEntity.list"
    protected static final String MASTER_READ_VIEW_ID = "test_ViewTemplateMasterEntity.read"

    @Autowired
    DataManager dataManager

    @Autowired
    ViewNavigators navigators

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_VIEW_TEMPLATE_LINE")
        jdbcTemplate.execute("delete from TEST_VIEW_TEMPLATE_MASTER")
        jdbcTemplate.execute("delete from TEST_VIEW_TEMPLATE_READ")
    }

    def "generated read view shows the stored entity in read-only fields"() {
        given: "an entity in the database"
        def entity = dataManager.create(ViewTemplateReadEntity)
        entity.name = "read-me"
        entity.active = true
        entity = dataManager.save(entity)

        when: "it is opened through read view resolution"
        navigators.readView(originView(), ViewTemplateReadEntity)
                .readEntity(entity)
                .navigate()

        then: "the generated read view is the one that opened"
        def view = UiTestUtils.currentView
        view instanceof TemplateReadView
        view.id.orElseThrow() == READ_VIEW_ID

        and: "the shown entity came from the view's own loader, not from the navigator's hand-off"
        // Asserting on getEntity() would pass even with a broken loader or fetch plan: it falls back to
        // the instance the navigator was handed.
        def container = ViewControllerUtils.getViewData(view).getContainer("entityDc")
        container.itemOrNull != null
        container.item.name == "read-me"
        container.item.active

        and: "the generated fields are read-only, which nothing in the descriptor asks for"
        UiComponentUtils.getComponent(view, "nameField").readOnly
        UiComponentUtils.getComponent(view, "activeField").readOnly
    }

    def "read action of a generated collection grid stays usable in the read-only view"() {
        given: "a master entity in the database"
        def master = dataManager.create(ViewTemplateMasterEntity)
        master.name = "master"
        master = dataManager.save(master)

        when: "its generated read view is open"
        navigators.readView(originView(), ViewTemplateMasterEntity)
                .readEntity(master)
                .navigate()

        def view = UiTestUtils.currentView
        def grid = UiComponentUtils.getComponent(view, "linesDataGrid")

        then: "the grid carries that one action and nothing else"
        grid.actions*.id == ["readAction"]

        and: "with no row selected it is disabled by the ordinary selection rule"
        !grid.getAction("readAction").enabled

        when: "a line is selected"
        def line = dataManager.create(ViewTemplateLineEntity)
        line.description = "line-1"
        line.master = master
        ViewControllerUtils.getViewData(view).getContainer("linesDc").mutableItems.add(line)
        grid.select(line)

        then: "the action is enabled, so the read-only state left the only way into a line alone"
        grid.getAction("readAction").enabled
    }

    def "read action of a generated collection grid opens the line read-only"() {
        given: "a master entity with a line, both stored"
        def master = dataManager.create(ViewTemplateMasterEntity)
        master.name = "master"

        def line = dataManager.create(ViewTemplateLineEntity)
        line.description = "line-1"
        line.quantity = 2
        line.master = master

        dataManager.save(master, line)

        and: "its generated read view is open with the line selected"
        navigators.readView(originView(), ViewTemplateMasterEntity)
                .readEntity(master)
                .navigate()

        def view = UiTestUtils.currentView

        // The grid lives in the second tab, and a tab sheet only attaches the content of the selected
        // tab: until the tab is selected the grid is not part of the view, and an action performed on it
        // fails with "not attached to a view".
        UiComponentUtils.getComponent(view, "contentTabSheet").setSelectedIndex(1)

        def grid = UiComponentUtils.getComponent(view, "linesDataGrid")
        def linesDc = ViewControllerUtils.getViewData(view).getContainer("linesDc")
        grid.select(linesDc.items.find { it.id == line.id })

        when: "the action is performed"
        grid.getAction("readAction").actionPerform(grid)

        then: "the line opens in a dialog, read-only, instead of failing on the read view's data context"
        // The read view's own DataContext is a NoopDataContext, which cannot be a parent of the opened
        // view's context. The line has no read view of its own, so resolution falls back to its
        // generated detail view and switches that one to read-only.
        def dialogView = UiTestUtils.lastOpenedViewDialog
        dialogView != null
        (dialogView as ReadOnlyAwareView).readOnly
        ViewControllerUtils.getViewData(dialogView).getContainer("entityDc").item.id == line.id
    }

    /**
     * Navigates to the generated list view of the read fixture and returns it, so a navigator has an
     * origin. Which view it is does not matter to read view resolution.
     */
    protected originView() {
        navigationSupport.navigate(READ_LIST_VIEW_ID)
        return UiTestUtils.currentView
    }
}
