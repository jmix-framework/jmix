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

package io.jmix.core.security.impl;

import io.jmix.core.security.AddonAuthenticationManagerProvider;
import io.jmix.core.security.AuthenticationManagerProvider;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;

public class AuthenticationManagerProviderImpl implements AuthenticationManagerProvider {

    protected List<AddonAuthenticationManagerProvider> providers;

    public AuthenticationManagerProviderImpl(List<AddonAuthenticationManagerProvider> providers) {
        this.providers = providers;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        //return AuthenticationProvider from the provider with the highest order
        return providers.get(0).getAuthenticationManager();
    }
}
