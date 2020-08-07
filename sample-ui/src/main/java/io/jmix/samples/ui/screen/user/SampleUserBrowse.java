/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.ui.screen.user;

import com.google.common.collect.Lists;
import io.jmix.samples.ui.entity.SampleUser;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("sample_User.browse")
@UiDescriptor("sample-user-browse.xml")
@LookupComponent("usersTable")
//@LoadDataBeforeShow
public class SampleUserBrowse extends StandardLookup<SampleUser> {

    @Autowired
    private CollectionContainer<SampleUser> usersDc;

    @Subscribe
    private void onInit(InitEvent event) {
        SampleUser user1 = new SampleUser();
        user1.setEnabled(true);
        user1.setFirstName("asd");
        user1.setLastName("asxcvcx");

        SampleUser user2 = new SampleUser();
        user2.setEnabled(true);
        user2.setFirstName("asd");
        user2.setLastName("asxcvcx");

        usersDc.setItems(Lists.newArrayList(user1, user2));
    }

}
