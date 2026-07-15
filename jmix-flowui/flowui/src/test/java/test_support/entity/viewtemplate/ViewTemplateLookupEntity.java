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

package test_support.entity.viewtemplate;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.view.template.DetailViewTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;
import test_support.entity.lookup_field.LfProduct;

/**
 * Test entity used to verify that a detail view generated from a template renders a
 * {@code @LookupField}-annotated reference ({@link LfProduct}, which carries a class-level
 * {@code @LookupField(type = DROPDOWN, itemsQuery = @LookupItemsQuery(byInstanceName = true))})
 * as an {@code entityComboBox} with a {@code byInstanceName} {@code itemsQuery}, end to end.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_LOOKUP_ENTITY")
@Entity(name = "test_ViewTemplateLookupEntity")
@DetailViewTemplate(
        viewId = "test_ViewTemplateLookupEntity.edit",
        viewRoute = "templates/view-template-lookup/detail"
)
public class ViewTemplateLookupEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    protected LfProduct product;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LfProduct getProduct() {
        return product;
    }

    public void setProduct(LfProduct product) {
        this.product = product;
    }
}
