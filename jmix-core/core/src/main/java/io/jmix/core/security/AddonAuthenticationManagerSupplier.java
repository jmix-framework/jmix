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

package io.jmix.core.security;

import org.springframework.security.authentication.AuthenticationManager;

/**
 * Implementations of the interface are responsible for constructing a global {@link AuthenticationManager}.
 * <p>
 * Several add-ons may provide an instance of {@link AddonAuthenticationManagerSupplier}. The AuthenticationManager
 * produced by the instance with the highest order will be used in the application as a global AuthenticationManager.
 *
 * @see AuthenticationManagerSupplier
 */
public interface AddonAuthenticationManagerSupplier {

    /**
     * Returns a global AuthenticationManager instance provided by an add-on.
     *
     * @return authentication manager
     */
    AuthenticationManager getAuthenticationManager();

}
