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

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.view.template.DetailViewTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;
import test_support.entity.sales.Customer;

import java.util.List;

/**
 * Parent entity with a composition collection ({@code lines}) and an association collection
 * ({@code relatedCustomers}), used to verify that only composition collections become tabs
 * in the generated detail view.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_MASTER")
@Entity(name = "test_ViewTemplateMasterEntity")
@DetailViewTemplate(viewId = "test_ViewTemplateMasterEntity.detail")
public class ViewTemplateMasterEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "master")
    protected List<ViewTemplateLineEntity> lines;

    @ManyToMany
    @JoinTable(name = "TEST_VIEW_TEMPLATE_MASTER_CUSTOMER_LINK")
    protected List<Customer> relatedCustomers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ViewTemplateLineEntity> getLines() {
        return lines;
    }

    public void setLines(List<ViewTemplateLineEntity> lines) {
        this.lines = lines;
    }

    public List<Customer> getRelatedCustomers() {
        return relatedCustomers;
    }

    public void setRelatedCustomers(List<Customer> relatedCustomers) {
        this.relatedCustomers = relatedCustomers;
    }
}
