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

import io.jmix.core.security.AddonAuthenticationManagerSupplier;
import io.jmix.core.security.AuthenticationManagerSupplier;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;

public class AuthenticationManagerSupplierImpl implements AuthenticationManagerSupplier {

    protected List<AddonAuthenticationManagerSupplier> suppliers;

    public AuthenticationManagerSupplierImpl(List<AddonAuthenticationManagerSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        //return AuthenticationProvider from the supplier with the highest order
        return suppliers.get(0).getAuthenticationManager();
    }
}
