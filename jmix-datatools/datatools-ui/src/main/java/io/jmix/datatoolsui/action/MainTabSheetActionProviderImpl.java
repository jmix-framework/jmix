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

package io.jmix.datatoolsui.action;

import io.jmix.ui.Actions;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.MainTabSheetActionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("datatl_ScreenContextMenuActionProvider")
public class MainTabSheetActionProviderImpl implements MainTabSheetActionProvider {

    @Autowired
    protected Actions actions;

    @Override
    public List<Action.MainTabSheetAction> getActions() {
        return Collections.singletonList(
                (EditorScreenShowEntityInfoAction) actions.create(EditorScreenShowEntityInfoAction.ID));
    }
}
