/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.lookupscreenfacet.screens;

import com.haulmont.cuba.core.model.common.User;
import io.jmix.core.Metadata;
import io.jmix.ui.actions.Action;
import io.jmix.ui.components.Button;
import io.jmix.ui.components.LookupScreenFacet;
import io.jmix.ui.components.PickerField;
import io.jmix.ui.components.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import spec.haulmont.cuba.web.user.screens.UserBrowseTest;

import javax.inject.Inject;
import java.util.Collection;

@SuppressWarnings({"unused"})
@UiController("cuba_LookupScreenFacetTestScreen")
@UiDescriptor("lookup-screen-facet-test-screen.xml")
public class LookupScreenFacetTestScreen extends Screen {

    @Inject
    public Metadata metadata;

    @Inject
    public CollectionContainer<User> userDc;

    @Inject
    public PickerField<User> pickerField;
    @Inject
    public Table<User> usersTable;
    @Inject
    public Button button;

    @Inject
    public Action action;

    @Inject
    public LookupScreenFacet<User, UserBrowseTest> lookupScreen;
    @Inject
    public LookupScreenFacet<User, UserBrowseTest> tableLookupScreen;
    @Inject
    public LookupScreenFacet<User, UserBrowseTest> fieldLookupScreen;

    @Subscribe
    public void onInit(InitEvent event) {
        User testUser = metadata.create(User.class);
        testUser.setName("Test user");

        pickerField.setValue(testUser);
    }

    @Install(to = "lookupScreen", subject = "selectHandler")
    public void onLookupSelect(Collection<User> selected) {
    }

    @Install(to = "lookupScreen", subject = "selectValidator")
    public boolean validateSelection(LookupScreen.ValidationContext<User> selected) {
        return true;
    }

    @Install(to = "lookupScreen", subject = "transformation")
    public Collection<User> transformLookupSelection(Collection<User> selected) {
        return selected;
    }

}
