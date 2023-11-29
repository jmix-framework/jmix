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

package io.jmix.simplesecurityflowui.access;

import java.util.Collection;

/**
 * Interface is used for managing access to views.
 */
//todo MG rename? ViewAccessRegistry? ViewPermissionsRegistry
public interface ViewAccessManager {

    /**
     * Grants access to view for specified role. A role name may be either with or without default role prefix, i.e.
     * both "ROLE_USER" and "USER" are valid.
     *
     * @param roleName role name
     * @param viewId   view id
     */
    void grantAccess(String roleName, String viewId);

    /**
     * Grants access to multiple views for specified role. A role name may be either with or without default role
     * prefix, i.e. both "ROLE_USER" and "USER" are valid.
     *
     * @param roleName role name
     * @param viewIds  a collection of view ids
     */
    void grantAccess(String roleName, Collection<String> viewIds);

    /**
     * Grants anonymous access to the view.
     *
     * @param viewId view id
     */
    void grantAnonymousAccess(String viewId);

    /**
     * Checks that current user has access to the view.
     *
     * @param viewId view id
     */
    boolean currentUserHasAccess(String viewId);

    /**
     * Checks that a given role has access to the view. A role name may be either with or without default role prefix,
     * i.e. both "ROLE_USER" and "USER" are valid.
     *
     * @param roleName role name
     * @param viewId   view id
     */
    boolean roleHasAccess(String roleName, String viewId);
}