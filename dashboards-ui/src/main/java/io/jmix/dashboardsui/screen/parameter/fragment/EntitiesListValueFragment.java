/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.screen.parameter.fragment;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.dashboards.model.parameter.type.EntityParameterValue;
import io.jmix.dashboards.model.parameter.type.ListEntitiesParameterValue;
import io.jmix.dashboards.model.parameter.type.ParameterValue;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Window;
import io.jmix.ui.model.KeyValueCollectionContainer;
import io.jmix.ui.screen.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@UiController("dshbrd_EntitiesListValue.fragment")
@UiDescriptor("entities-list-value-fragment.xml")
public class EntitiesListValueFragment extends ScreenFragment implements ValueFragment {
    @Autowired
    protected KeyValueCollectionContainer entitiesDc;

    @Autowired
    protected Screens screens;

    @Autowired
    private Metadata metadata;

    protected Map<KeyValueEntity, EntityParameterValue> tableValues = new HashMap<>();
    protected KeyValueEntity oldValue;

    @Subscribe
    public void onInit(InitEvent event) {
        MapScreenOptions options = (MapScreenOptions) event.getOptions();
        Map<String, Object> params = options.getParams();

        initDc(params);
    }

    @Override
    public ParameterValue getValue() {
        return new ListEntitiesParameterValue(new ArrayList<>(tableValues.values()));
    }

    protected void initDc(Map<String, Object> params) {
        ListEntitiesParameterValue value = (ListEntitiesParameterValue) params.get(VALUE);

        if (value == null || value.getValue() == null) {
            value = new ListEntitiesParameterValue();
        }

        for (EntityParameterValue entityValue : value.getValue()) {
            KeyValueEntity keyValueEntity = createKeyValueEntity(entityValue);
            entitiesDc.getMutableItems().add(keyValueEntity);
            tableValues.put(keyValueEntity, entityValue);
        }
    }

    @Subscribe("entitiesTable.create")
    public void createEntityValue(Action.ActionPerformedEvent event) {
        oldValue = null;
        openEntityValueScreen(null);
    }

    @Subscribe("entitiesTable.edit")
    public void editEntityValue(Action.ActionPerformedEvent event) {
        KeyValueEntity item = entitiesDc.getItem();
        if (item != null) {
            oldValue = item;
            openEntityValueScreen(tableValues.get(item));
        }
    }

    @Subscribe("entitiesTable.remove")
    public void removeEntityValue(Action.ActionPerformedEvent event) {
        KeyValueEntity item = entitiesDc.getItem();
        if (item != null) {
            entitiesDc.getMutableItems().remove(item);
            tableValues.remove(item);
        }
    }

    protected void openEntityValueScreen(EntityParameterValue value) {
        screens.create(EntityValueScreen.class, OpenMode.DIALOG, new MapScreenOptions(ParamsMap.of(VALUE, value)))
                .show()
                .addAfterCloseListener(e -> {
                    StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                    if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                        saveWindowValue((EntityParameterValue) ((EntityValueScreen) e.getScreen()).getValue());
                    }
                });
    }

    protected void saveWindowValue(EntityParameterValue windowValue) {
        if (oldValue != null) {
            entitiesDc.getMutableItems().remove(oldValue);
            tableValues.remove(oldValue);
            oldValue = null;
        }

        KeyValueEntity newValue = EntitiesListValueFragment.this.createKeyValueEntity(windowValue);
        entitiesDc.getMutableItems().add(newValue);
        tableValues.put(newValue, windowValue);
    }

    protected KeyValueEntity createKeyValueEntity(EntityParameterValue value) {
        KeyValueEntity keyValueEntity = metadata.create(KeyValueEntity.class);
        keyValueEntity.setValue("metaClassName", value.getMetaClassName());
        keyValueEntity.setValue("entityId", value.getEntityId());
        keyValueEntity.setValue("fetchPlanName", value.getFetchPlan());
        return keyValueEntity;
    }
}
