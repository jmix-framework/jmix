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

package io.jmix.rest.api.service;

import io.jmix.core.entity.User;
import io.jmix.core.security.UserSessionSource;
import io.jmix.rest.api.controllers.UserInfoController;
import io.jmix.rest.api.service.filter.data.UserInfo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Class that is used by the {@link UserInfoController} for getting an information
 * about the current user
 */
@Component("jmix_UserInfoControllerManager")
public class UserInfoControllerManager {

    @Inject
    protected UserSessionSource userSessionSource;

    public UserInfo getUserInfo() {
        // todo user substitution
//        User user = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
        User user = userSessionSource.getUserSession().getUser();
        UserInfo userInfo = new UserInfo(user);
        userInfo.setLocale(userSessionSource.getUserSession().getLocale().toString());
        return userInfo;
    }
}
