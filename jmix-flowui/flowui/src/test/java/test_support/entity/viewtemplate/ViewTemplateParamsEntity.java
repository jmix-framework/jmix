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
import io.jmix.flowui.view.template.ListViewTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;

/**
 * Test entity used to verify template parameter propagation.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_PARAMS")
@Entity(name = "test_ViewTemplateParamsEntity")
@ListViewTemplate(
        path = "view_template/params-list-view.ftl",
        viewId = "test_ViewTemplateParamsEntity.browse",
        viewTitle = "Params entity",
        templateParams = """
                {"titleSuffix":"from params","markerText":"params marker"}
                """
)
public class ViewTemplateParamsEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

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
}
