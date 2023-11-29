/*
 * Copyright 2022 Haulmont.
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

package io.jmix.simplesecurityflowui.access.impl;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.simplesecurity.SimpleSecurityProperties;
import io.jmix.simplesecurity.role.GrantedAuthorityUtils;
import io.jmix.simplesecurityflowui.access.ViewAccessManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ViewAccessManager}.
 */
@Component("simsec_ViewAccessManager")
public class ViewAccessManagerImpl implements ViewAccessManager {

    protected Map<String, Set<String>> roleToViewsMap = new ConcurrentHashMap<>();

    protected Set<String> anonymousViews = new CopyOnWriteArraySet<>();

    protected CurrentAuthentication currentAuthentication;

    protected final SimpleSecurityProperties simpleSecurityProperties;

    protected final GrantedAuthorityUtils grantedAuthorityUtils;

    public ViewAccessManagerImpl(CurrentAuthentication currentAuthentication,
                                 SimpleSecurityProperties simpleSecurityProperties,
                                 GrantedAuthorityUtils grantedAuthorityUtils) {
        this.currentAuthentication = currentAuthentication;
        this.simpleSecurityProperties = simpleSecurityProperties;
        this.grantedAuthorityUtils = grantedAuthorityUtils;
    }

    @Override
    public void grantAccess(String roleName, String viewId) {
        String roleWithDefaultPrefix = grantedAuthorityUtils.getRoleWithDefaultPrefix(roleName);
        Set<String> viewIds = roleToViewsMap.getOrDefault(roleWithDefaultPrefix, new HashSet<>());
        viewIds.add(viewId);
        roleToViewsMap.put(roleWithDefaultPrefix, viewIds);
    }

    @Override
    public void grantAccess(String roleName, Collection<String> viewIds) {
        for (String viewId : viewIds) {
            grantAccess(roleName, viewId);
        }
    }

    @Override
    public void grantAnonymousAccess(String viewId) {
        anonymousViews.add(viewId);
    }

    @Override
    public boolean currentUserHasAccess(String viewId) {
        if (anonymousViews.contains(viewId)) return true;

        Set<String> currentUserRoles = currentAuthentication.getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String adminRoleName = simpleSecurityProperties.getAdminRole();
        String prefixedAdminRole = grantedAuthorityUtils.getRoleWithDefaultPrefix(adminRoleName);
        if (currentUserRoles.contains(prefixedAdminRole)) {
            return true;
        }

        for (String roleName : currentUserRoles) {
            if (roleHasAccess(roleName, viewId)) return true;
        }

        return false;
    }

    @Override
    public boolean roleHasAccess(String roleName, String viewId) {
        if (anonymousViews.contains(viewId)) return true;
        String roleWithDefaultPrefix = grantedAuthorityUtils.getRoleWithDefaultPrefix(roleName);
        Set<String> viewIds = roleToViewsMap.getOrDefault(roleWithDefaultPrefix, Collections.emptySet());
        return viewIds.contains(viewId);
    }
}
