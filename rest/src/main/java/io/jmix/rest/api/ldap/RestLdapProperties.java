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

package io.jmix.rest.api.ldap;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.rest.ldap")
@ConstructorBinding
public class RestLdapProperties {

    boolean enabled;
    List<String> urls;
    String base;
    String user;
    String password;
    String userLoginField;

    public RestLdapProperties(@DefaultValue("false") boolean enabled,
                              @DefaultValue("") List<String> urls,
                              String base,
                              String user,
                              String password,
                              @DefaultValue("sAMAccountName") String userLoginField) {
        this.enabled = enabled;
        this.urls = urls;
        this.base = base;
        this.user = user;
        this.password = password;
        this.userLoginField = userLoginField;
    }

    /**
     * @return true if LDAP authentication for REST API is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the urls of the LDAP servers
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * @return the base LDAP suffix from which all operations should origin.
     * If a base suffix is set, you will not have to (and, indeed, must not) specify the full distinguished names in any
     * operations performed. For instance: dc=example,dc=com
     */
    public String getBase() {
        return base;
    }

    /**
     * @return user that is used to connect to LDAP server.
     * For instance: cn=System User,ou=Employees,dc=mycompany,dc=com
     */
    public String getUser() {
        return user;
    }

    /**
     * @return password that is used to connect to LDAP server
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Field of LDAP object for user login matching.
     */
    public String getUserLoginField() {
        return userLoginField;
    }
}
