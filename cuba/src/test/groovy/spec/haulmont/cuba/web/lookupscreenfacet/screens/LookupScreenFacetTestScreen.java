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
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.LookupScreenFacet;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import spec.haulmont.cuba.web.user.screens.UserBrowseTest;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;

@SuppressWarnings({"unused"})
@UiController("cuba_LookupScreenFacetTestScreen")
@UiDescriptor("lookup-screen-facet-test-screen.xml")
public class LookupScreenFacetTestScreen extends Screen {

    @Autowired
    public Metadata metadata;

    @Autowired
    public CollectionContainer<User> userDc;

    @Autowired
    public EntityPicker<User> userPicker;
    @Autowired
    public Table<User> usersTable;
    @Autowired
    public Button button;

    @Autowired
    public Action action;

    @Autowired
    public LookupScreenFacet<User, UserBrowseTest> lookupScreen;
    @Autowired
    public LookupScreenFacet<User, UserBrowseTest> tableLookupScreen;
    @Autowired
    public LookupScreenFacet<User, UserBrowseTest> fieldLookupScreen;

    @Subscribe
    public void onInit(InitEvent event) {
        User testUser = metadata.create(User.class);
        testUser.setName("Test user");

        userPicker.setValue(testUser);
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
