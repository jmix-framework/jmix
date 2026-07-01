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
import io.jmix.flowui.view.template.ListViewTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;

/**
 * Test entity used to verify list and detail views generated from templates.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_ENTITY")
@Entity(name = "test_ViewTemplateEntity")
@ListViewTemplate(
        parentMenu = "templateViews",
        viewRoute = "templates/view-template/list"
)
@DetailViewTemplate(
        parentMenu = "templateViews",
        viewId = "test_ViewTemplateEntity.edit",
        viewTitle = "Template entity editor",
        viewRoute = "templates/view-template/detail"
)
public class ViewTemplateTestEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

    @Column(name = "ACTIVE")
    protected Boolean active;

    /**
     * @return entity name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the entity name.
     *
     * @param name entity name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return active flag
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the active flag.
     *
     * @param active active flag
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
}
