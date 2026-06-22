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
 * Test entity used to verify that {@code viewTitle} values starting with {@code msg://}
 * are resolved through the message bundle. The list view uses the full reference format
 * ({@code msg://group/key}) and the detail view uses the brief format ({@code msg://key})
 * resolved against the entity package.
 */
@JmixEntity
@Table(name = "TEST_VIEW_TEMPLATE_MSG_TITLE")
@Entity(name = "test_ViewTemplateMsgTitleEntity")
@ListViewTemplate(
        parentMenu = "templateViews",
        viewId = "test_ViewTemplateMsgTitleEntity.list",
        viewTitle = "msg://test_support/viewTemplate.msgTitleList"
)
@DetailViewTemplate(
        parentMenu = "templateViews",
        viewId = "test_ViewTemplateMsgTitleEntity.detail",
        viewTitle = "msg://viewTemplate.msgTitleDetail"
)
public class ViewTemplateMsgTitleEntity extends TestBaseEntity {

    @Column(name = "NAME")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
