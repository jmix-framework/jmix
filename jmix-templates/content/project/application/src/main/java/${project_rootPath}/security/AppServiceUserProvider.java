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

package ${project_rootPackage}.security;

import io.jmix.core.security.user.DefaultServiceUserProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * The class is responsible for initialization of system and anonymous users.
 */
@Component("${normalizedPrefix_underscore}AppServiceUserProvider")
public class AppServiceUserProvider extends DefaultServiceUserProvider {

    @Override
    protected Collection<GrantedAuthority> getSystemUserAuthorities() {
        return Collections.emptyList();
    }

    @Override
    protected Collection<GrantedAuthority> getAnonymousUserAuthorities() {
        return Collections.emptyList();
    }
}