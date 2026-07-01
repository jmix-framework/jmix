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

/**
 * Composition line entity used to verify composition collection support in the detail view template.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_LINE")
@Entity(name = "test_ViewTemplateLineEntity")
@DetailViewTemplate(viewId = "test_ViewTemplateLineEntity.detail")
public class ViewTemplateLineEntity extends TestBaseEntity {

    @Column(name = "DESCRIPTION")
    protected String description;

    @Column(name = "QUANTITY")
    protected Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASTER_ID")
    protected ViewTemplateMasterEntity master;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ViewTemplateMasterEntity getMaster() {
        return master;
    }

    public void setMaster(ViewTemplateMasterEntity master) {
        this.master = master;
    }
}
