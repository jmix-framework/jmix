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
import io.jmix.flowui.view.template.ReadViewTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;

/**
 * Test entity used to verify a read view generated from a template with every default in place. The list
 * template supplies the navigation origin and the menu comparison.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_READ")
@Entity(name = "test_ViewTemplateReadEntity")
@ListViewTemplate(parentMenu = "templateViews")
// The list template's create and edit actions target the entity's detail view, so the fixture declares
// one: without it the generated list view is half-broken and a later test would fail on the fixture.
@DetailViewTemplate
@ReadViewTemplate
public class ViewTemplateReadEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

    @Column(name = "ACTIVE")
    protected Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
