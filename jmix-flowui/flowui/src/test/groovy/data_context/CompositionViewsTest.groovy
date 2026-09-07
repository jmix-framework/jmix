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

package data_context

import com.vaadin.flow.component.UI
import data_context.view.OrderView
import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.Metadata
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.view.navigation.UrlParamSerializer
import io.jmix.flowui.view.navigation.ViewNavigationSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Unroll
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.OrderLineParam
import test_support.entity.sales.OrderLineParamNote
import test_support.spec.FlowuiTestSpecification

@SuppressWarnings("GroovyAssignabilityCheck")
@SpringBootTest
class CompositionViewsTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    ViewNavigators viewNavigators
    @Autowired
    ViewNavigationSupport navigationSupport
    @Autowired
    Metadata metadata
    @Autowired
    UrlParamSerializer urlParamSerializer

    @Override
    void setup() {
        registerViewBasePackages('data_context.view')
    }

    @Override
    void cleanup() {
        jdbcTemplate.update("delete from TEST_ORDER_LINE_PARAM_NOTE")
        jdbcTemplate.update("delete from TEST_ORDER_LINE_PARAM")
        jdbcTemplate.update("delete from TEST_ORDER_LINE")
        jdbcTemplate.update("delete from TEST_ORDER")
    }

    @Unroll
    def "create and immediate edit of the same nested instance"(boolean explicitParentDc) {

        def order = metadata.create(Order)
        dataManager.save(order)

        navigationSupport.navigate(OrderView, urlParamSerializer.serialize(order.id))
        OrderView orderView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        def orderScreenDc = orderView.viewData.dataContext

        when: "create entity"

        def lineScreenForCreate = orderView.buildLineScreenForCreate(explicitParentDc)

        lineScreenForCreate.changeSaveAndClose(1)

        then:

        def order1 = orderScreenDc.find(Order, order.id)
        order1.orderLines.size() == 1
        def line1 = order1.orderLines[0]
        line1.order.is(order1)

        when: "edit same entity"

        def lineScreenForEdit = orderView.buildLineScreenForEdit(explicitParentDc)

        lineScreenForEdit.changeSaveAndClose(2)

        then:

        def order2 = orderScreenDc.find(Order, order.id)
        order2.is(order1)
        order2.orderLines.size() == 1
        def line2 = order2.orderLines[0]
        line2.is(line1)
        line2.order.is(order2)

        where:

        explicitParentDc << [true, false]
    }

    def "a deep composition edit survives reopening the intermediate editor (#4907)"() {

        given: "an order with a line and a param, opened in the order view"

        def order = dataManager.save(new Order(number: '1', orderLines: []))
        def orderLine = dataManager.save(new OrderLine(quantity: 1, params: [], order: order))
        dataManager.save(new OrderLineParam(name: 'p1', value: 'v1', orderLine: orderLine))

        navigationSupport.navigate(OrderView, urlParamSerializer.serialize(order.id))
        OrderView orderView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        when: "the param is edited two levels down, then the param and line editors are saved while the " +
                "order view stays open"

        def lineView = orderView.buildLineScreenForEdit(false)
        def paramView = lineView.buildParamViewForEdit()
        paramView.changeValueSaveAndClose('v2')
        lineView.closeWithSave()

        then: "reopening the line editor shows the edited param instead of a stale copy from the database"

        def reopenedLineView = orderView.buildLineScreenForEdit(false)
        reopenedLineView.paramsDc.items.size() == 1
        reopenedLineView.paramsDc.items[0].value == 'v2'
    }

    def "a deep composition edit survives reopening editors two levels up (#4907)"() {

        given: "an order with a line, a param and a note, opened in the order view"

        def order = dataManager.save(new Order(number: '1', orderLines: []))
        def orderLine = dataManager.save(new OrderLine(quantity: 1, params: [], order: order))
        def lineParam = dataManager.save(
                new OrderLineParam(name: 'p1', value: 'v1', orderLine: orderLine, notes: []))
        def note = dataManager.save(new OrderLineParamNote(text: 't1', param: lineParam))

        navigationSupport.navigate(OrderView, urlParamSerializer.serialize(order.id))
        OrderView orderView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        when: "the note is edited three levels down, then every editor but the order view is saved"

        def lineView = orderView.buildLineScreenForEdit(false)
        def paramView = lineView.buildParamViewForEdit()
        def noteView = paramView.buildNoteViewForEdit()
        noteView.changeTextSaveAndClose('t2')
        paramView.closeWithSave()
        lineView.closeWithSave()

        then: "reopening the line editor and the param editor in it shows the edited note"

        def reopenedLineView = orderView.buildLineScreenForEdit(false)
        def reopenedParamView = reopenedLineView.buildParamViewForEdit()
        reopenedParamView.notesDc.items.size() == 1
        reopenedParamView.notesDc.items[0].text == 't2'

        when: "the order view saves the aggregate"

        orderView.viewData.dataContext.save()

        then: "the deep edit reaches the database"

        dataManager.load(Id.of(note)).one().text == 't2'
    }

    def "remove nested instance on 2nd level"() {

        def order = dataManager.save(new Order(number: '1', orderLines: []))

        def orderLine = dataManager.save(new OrderLine(quantity: 1, params: [], order: order))

        def lineParam = dataManager.save(new OrderLineParam(name: 'p1', orderLine: orderLine))

        navigationSupport.navigate(OrderView, urlParamSerializer.serialize(order.id))
        OrderView orderView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        def orderViewCtx = orderView.viewData.dataContext

        when:

        def lineView = orderView.buildLineScreenForEdit(false)

        def lineViewCtx = lineView.viewData.dataContext

        then:

        lineViewCtx.parent == orderViewCtx
        lineView.paramsDc.items.contains(lineParam)

        when:

        def lineParam1 = lineViewCtx.find(lineParam)
        lineView.paramsDc.getMutableItems().remove(lineParam1)
        lineViewCtx.remove(lineParam1)
        lineViewCtx.save()

        then:

        orderViewCtx.isRemoved(lineParam)

        cleanup:

        dataManager.remove(lineParam, orderLine, order)
    }

    def "reverting a line attribute in a second dialog session leaves the order view with nothing to save"() {

        given: "an order with a line, opened in the order view"

        def order = dataManager.save(new Order(number: '1', orderLines: []))
        def orderLine = dataManager.save(new OrderLine(quantity: 1, params: [], order: order))

        navigationSupport.navigate(OrderView, urlParamSerializer.serialize(order.id))
        OrderView orderView = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        def orderViewCtx = orderView.viewData.dataContext

        when: "a first dialog session changes the quantity"

        orderView.buildLineScreenForEdit(false).changeSaveAndClose(2)

        then: "the order view holds the edit and reports it as unsaved"

        orderViewCtx.find(OrderLine, orderLine.id).quantity == 2
        !orderViewCtx.getModified().empty

        when: "a second dialog session sets the quantity back to the persisted value"

        orderView.buildLineScreenForEdit(false).changeSaveAndClose(1)

        then: "the line matches the database again and the order view has nothing to save"

        orderViewCtx.find(OrderLine, orderLine.id).quantity == 1
        orderViewCtx.getModifiedAttributes(orderViewCtx.find(OrderLine, orderLine.id)).empty
        orderViewCtx.getModified().empty
    }
}
