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

package io.jmix.rest.impl.service;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.rest.impl.controller.UserInfoController;
import io.jmix.rest.impl.service.filter.data.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Class that is used by the {@link UserInfoController} for getting an information
 * about the current user
 */
@Component("rest_UserInfoControllerManager")
public class UserInfoControllerManager {

    private static final Logger log = LoggerFactory.getLogger(UserInfoControllerManager.class);

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    /**
     * @return all user attributes if the user stored in db implements {@link UserDetails}. Username and locale otherwise
     */
    public UserInfo getUserInfo() {
        UserDetails user = currentAuthentication.getUser();
        UserInfo userInfo = new UserInfo(user);
        userInfo.setLocale(currentAuthentication.getLocale().toString());

        MetaClass userMetaClass = metadata.findClass(user.getClass());
        Map<String, Object> attributes = userInfo.getAttributes();
        if (userMetaClass != null) {
            //using metadata we assume that the pojo contains only the "correct" field-getter-setter namings
            for (MetaProperty metaProperty : userMetaClass.getProperties()) {
                if (metadataTools.isAnnotationPresent(user, metaProperty.getName(), Secret.class)
                        || metaProperty.getRange().isClass()) {
                    continue;
                }
                try {
                    attributes.put(metaProperty.getName(), EntityValues.getValue(user, metaProperty.getName()));
                } catch (Exception e) {
                    log.warn(String.format("Can not get value for %s", metaProperty.getName()));
                }
            }
        }
        return userInfo;
    }
}
