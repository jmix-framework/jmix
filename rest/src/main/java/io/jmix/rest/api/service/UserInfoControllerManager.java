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

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.rest.api.controller.UserInfoController;
import io.jmix.rest.api.service.filter.data.UserInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class that is used by the {@link UserInfoController} for getting an information
 * about the current user
 */
@Component("rest_UserInfoControllerManager")
public class UserInfoControllerManager {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public UserInfo getUserInfo() {
        UserDetails user = currentAuthentication.getUser();
        UserInfo userInfo = new UserInfo(user);
        userInfo.setLocale(currentAuthentication.getLocale().toString());
        return userInfo;
    }
}
