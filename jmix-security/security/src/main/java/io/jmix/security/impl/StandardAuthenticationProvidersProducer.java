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

package io.jmix.security.impl;

import io.jmix.core.security.PostAuthenticationChecks;
import io.jmix.core.security.PreAuthenticationChecks;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.SubstitutedUserAuthenticationProvider;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Class returns a collection of "standard" providers that may be used in AuthenticationManagers created in different
 * security configurations.
 */
@Component("sec_StandardAuthenticationProvidersProducer")
public class StandardAuthenticationProvidersProducer {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private PreAuthenticationChecks preAuthenticationChecks;

    private PostAuthenticationChecks postAuthenticationChecks;

    public StandardAuthenticationProvidersProducer(UserRepository userRepository,
                                                   PasswordEncoder passwordEncoder,
                                                   @Qualifier("sec_PreAuthenticationChecks") PreAuthenticationChecks preAuthenticationChecks,
                                                   @Qualifier("sec_PostAuthenticationChecks") PostAuthenticationChecks postAuthenticationChecks) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.preAuthenticationChecks = preAuthenticationChecks;
        this.postAuthenticationChecks = postAuthenticationChecks;
    }

    public List<AuthenticationProvider> getStandardProviders() {
        List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(new SystemAuthenticationProvider(userRepository));
        providers.add(new SubstitutedUserAuthenticationProvider(userRepository));

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setPreAuthenticationChecks(preAuthenticationChecks);
        daoAuthenticationProvider.setPostAuthenticationChecks(postAuthenticationChecks);

        providers.add(daoAuthenticationProvider);
        return providers;
    }

}
