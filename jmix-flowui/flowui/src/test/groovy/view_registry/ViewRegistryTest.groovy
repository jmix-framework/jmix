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

package view_registry

import io.jmix.flowui.exception.NoSuchViewException
import io.jmix.flowui.view.ViewRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.Foo
import test_support.entity.sales.*
import test_support.spec.FlowuiTestSpecification
import view_registry.view.customer.CustomerDetailView
import view_registry.view.customer.CustomerLookupView
import view_registry.view.customer.CustomerPrimaryListView
import view_registry.view.order.OrderPrimaryListView
import view_registry.view.product.ProductListView
import view_registry.view.product.ProductPrimaryDetailView
import view_registry.view.product.ProductPrimaryLookupView
import view_registry.view.producttag.ProductTagListView

@SpringBootTest
class ViewRegistryTest extends FlowuiTestSpecification {

    @Autowired
    ViewRegistry viewRegistry;

    void setup() {
        registerScreenBasePackages("view_registry.view")
    }

    /* List view */

    def "find list view with @PrimaryListView"() {
        when:
        def viewInfo = viewRegistry.getListViewInfo(Customer)

        then:
        viewInfo.id == CustomerPrimaryListView.VIEW_ID
    }

    def "find list view with list view id convention"() {
        when:
        def viewInfo = viewRegistry.getListViewInfo(Product)

        then:
        viewInfo.id == ProductListView.VIEW_ID
    }

    def "no list view found"() {
        when:
        viewRegistry.getListViewInfo(Address)

        then:
        thrown(NoSuchViewException)
    }

    /* Lookup view */

    def "find lookup view with @PrimaryLookupView"() {
        when:
        def viewInfo = viewRegistry.getLookupViewInfo(Product)

        then:
        viewInfo.id == ProductPrimaryLookupView.VIEW_ID
    }

    def "find lookup view with lookup view id convention"() {
        when:
        def viewInfo = viewRegistry.getLookupViewInfo(Customer)

        then:
        viewInfo.id == CustomerLookupView.VIEW_ID
    }

    def "find lookup view with @PrimaryListView"() {
        when:
        def viewInfo = viewRegistry.getLookupViewInfo(Order)

        then:
        viewInfo.id == OrderPrimaryListView.VIEW_ID
    }

    def "find lookup view with list view id convention"() {
        when:
        def viewInfo = viewRegistry.getListViewInfo(ProductTag)

        then:
        viewInfo.id == ProductTagListView.VIEW_ID
    }

    def "no lookup view found"() {
        when:
        viewRegistry.getListViewInfo(Foo)

        then:
        thrown(NoSuchViewException)
    }

    /* Detail view */

    def "find detail view with @PrimaryDetailView"() {
        when:
        def viewInfo = viewRegistry.getDetailViewInfo(Product)

        then:
        viewInfo.id == ProductPrimaryDetailView.VIEW_ID
    }

    def "find detail view with detail view id convention"() {
        when:
        def viewInfo = viewRegistry.getDetailViewInfo(Customer)

        then:
        viewInfo.id == CustomerDetailView.VIEW_ID
    }

    def "no detail view found"() {
        when:
        viewRegistry.getDetailViewInfo(Order)

        then:
        thrown(NoSuchViewException)
    }
}
