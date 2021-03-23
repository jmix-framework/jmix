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

import io.jmix.dashboards.model.parameter.type.EntityParameterValue;
import io.jmix.dashboards.model.parameter.type.ParameterValue;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_EntityValue.screen")
@UiDescriptor("entity-value-screen.xml")
public class EntityValueScreen extends Screen implements ValueFragment, Window.Committable {

    @Autowired
    protected EntityValueFragment entityValueFragment;

    protected EntityParameterValue prevValue;

    @Subscribe
    public void onInit(InitEvent event) {
        MapScreenOptions options = (MapScreenOptions) event.getOptions();
        Map<String, Object> params = options.getParams();

        entityValueFragment.init(params);
        prevValue = (EntityParameterValue) entityValueFragment.getValue();
    }

    @Override
    public ParameterValue getValue() {
        return entityValueFragment.getValue();
    }

    @Subscribe("okBtn")
    public void commitAndClose(Button.ClickEvent event) {
        commitAndClose();
    }

    @Subscribe("cancelBtn")
    public void cancelAndClose(Button.ClickEvent event) {
        close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    @Override
    public boolean isModified() {
        EntityParameterValue currentVal = (EntityParameterValue) entityValueFragment.getValue();
        if ((prevValue == null && currentVal != null) || (prevValue != null && currentVal == null)) {
            return true;
        }
        if (prevValue != null) {
            return !prevValue.equals(currentVal);
        }
        return false;
    }

    @Override
    public void commitAndClose() {
        close(new StandardCloseAction(COMMIT_ACTION_ID, false));
    }
}
