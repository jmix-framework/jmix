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

package spec.haulmont.cuba.web.editorscreenfacet.screens;


import com.haulmont.cuba.core.model.common.User;
import io.jmix.core.Metadata;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EditorScreenFacet;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import spec.haulmont.cuba.web.user.screens.UserEditTest;

import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings({"unused"})
@UiController("cuba_EditorScreenFacetTestScreen")
@UiDescriptor("editor-screen-facet-test-screen.xml")
public class EditorScreenFacetTestScreen extends Screen {

    @Autowired
    public Metadata metadata;

    @Autowired
    public CollectionContainer<User> userDc;

    @Autowired
    public EntityPicker<User> userField;
    @Autowired
    public Table<User> usersTable;

    @Autowired
    public Action action;
    @Autowired
    public Button button;

    @Autowired
    public EditorScreenFacet<User, UserEditTest> editorScreenFacet;
    @Autowired
    public EditorScreenFacet<User, UserEditTest> tableScreenFacet;
    @Autowired
    public EditorScreenFacet<User, UserEditTest> fieldScreenFacet;
    @Autowired
    public EditorScreenFacet<User, UserEditTest> editorEntityProvider;

    @Install(to = "editorScreenFacet", subject = "entityProvider")
    public User provideEntity() {
        User testUser = metadata.create(User.class);
        testUser.setName("Test user");

        return testUser;
    }

    @Install(to = "editorEntityProvider", subject = "entityProvider")
    public User provideUser() {
        User testUser = metadata.create(User.class);
        testUser.setName("Test user");

        return testUser;
    }

    @Install(to = "editorScreenFacet", subject = "initializer")
    public void initEntity(User user) {
        user.setEmail("test@email.com");
    }
}
