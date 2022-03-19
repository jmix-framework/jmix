/*
 * Copyright 2021 Haulmont.
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

package io.jmix.security.configurer;

public class StandardSecurityConfigurer extends CompositeConfigurer {
    public StandardSecurityConfigurer() {
        addConfigurer(new DefaultConfigurer());
    }

    public StandardSecurityConfigurer anonymous() {
        addConfigurer(new AnonymousConfigurer());
        return this;
    }

    public StandardSecurityConfigurer sessionManagement() {
        addConfigurer(new SessionManagementConfigurer());
        return this;
    }

    public StandardSecurityConfigurer allowApiUrls() {
        addConfigurer(new AuthorizedApiUrlsConfigurer());
        return this;
    }

    public StandardSecurityConfigurer allowUiUrls() {
        addConfigurer(new UiUrlsConfigurer());
        return this;
    }

    public StandardSecurityConfigurer rememberMe() {
        addConfigurer(new RememberMeConfigurer());
        return this;
    }

}
