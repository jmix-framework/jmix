/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securityui.screen.rowlevelpolicy;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import io.jmix.securityui.model.RowLevelPolicyModel;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@UiController("sec_RowLevelPolicyModel.edit")
@UiDescriptor("row-level-policy-model-edit.xml")
@EditedEntityContainer("rowLevelPolicyModelDc")
public class RowLevelPolicyModelEdit extends StandardEditor<RowLevelPolicyModel> {

    @Autowired
    private ComboBox<String> entityNameField;

    @Autowired
    private Metadata metadata;

    @Autowired
    private TextArea<String> scriptField;

    @Autowired
    private TextArea<String> whereClauseField;

    @Autowired
    private MessageTools messageTools;

    @Autowired
    private TextArea<String> joinClauseField;

    @Autowired
    private ComboBox actionField;

    public Map<String, String> getEntityOptionsMap() {
        return metadata.getClasses().stream()
                .collect(Collectors.toMap(
                        this::getEntityCaption,
                        MetaClass::getName,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    @Subscribe(id = "rowLevelPolicyModelDc", target = Target.DATA_CONTAINER)
    private void onRowLevelPolicyModelDcItemPropertyChange(
            InstanceContainer.ItemPropertyChangeEvent<RowLevelPolicyModel> event) {
        if ("type".equals(event.getProperty())) {
            initFieldsAccessForType((RowLevelPolicyType) event.getValue());
        }
    }

    private void initFieldsAccessForType(@Nullable RowLevelPolicyType type) {
        if (type == null) return;
        switch (type) {
            case JPQL:
                scriptField.setVisible(false);
                whereClauseField.setVisible(true);
                joinClauseField.setVisible(true);
                actionField.setEditable(false);
                getEditedEntity().setAction(RowLevelPolicyAction.READ);
                getEditedEntity().setScript(null);
                break;
            case PREDICATE:
                scriptField.setVisible(true);
                whereClauseField.setVisible(false);
                joinClauseField.setVisible(false);
                actionField.setEditable(true);
                getEditedEntity().setWhereClause(null);
                getEditedEntity().setJoinClause(null);
                break;
        }
    }

    private String getEntityCaption(MetaClass metaClass) {
        return String.format("%s (%s)", messageTools.getEntityCaption(metaClass), metaClass.getName());
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        entityNameField.setOptionsMap(getEntityOptionsMap());
        initFieldsAccessForType(getEditedEntity().getType());
    }
}