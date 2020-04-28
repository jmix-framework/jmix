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

package io.jmix.rest.api.controllers;

import io.jmix.rest.api.service.PermissionsControllerManager;
import io.jmix.rest.api.service.filter.data.EffectiveRoleInfo;
import io.jmix.rest.api.service.filter.data.PermissionInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Controller that is used for getting current user permissions
 */
@RestController("jmix_PermissionsController")
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PermissionsController {

    @Inject
    protected PermissionsControllerManager permissionsControllerManager;

    @GetMapping("/v2/permissions")
    public Collection<PermissionInfo> getPermissions() {
        //todo security
//        return permissionsControllerManager.getPermissionInfos();
        return null;
    }

    @GetMapping("/v2/permissions/effective")
    public EffectiveRoleInfo getEffectiveRole(@RequestParam(required = false) boolean entities,
                                              @RequestParam(required = false) boolean entityAttributes,
                                              @RequestParam(required = false) boolean specific) {
        PermissionsControllerManager.EffectiveRoleRequestParams params =
                new PermissionsControllerManager.EffectiveRoleRequestParams();
        params.setEntities(entities);
        params.setEntityAttributes(entityAttributes);
        params.setSpecific(specific);
        //todo security
//        return permissionsControllerManager.getEffectiveRole(params);
        return null;
    }

}
