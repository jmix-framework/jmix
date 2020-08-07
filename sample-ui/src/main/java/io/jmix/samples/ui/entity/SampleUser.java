/*
 * Copyright 2019 Haulmont.
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
package io.jmix.samples.ui.entity;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.data.entity.StandardEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

/**
 * User
 */
@Entity(name = "sample_User")
@Table(name = "SAMPLE_USER")
//@Listeners("jmix_UserEntityListener")
public class SampleUser extends StandardEntity implements BaseUser {

    private static final long serialVersionUID = 5007187642916030394L;

    @Column(name = "USERNAME", nullable = false)
    protected String username;

    @SystemLevel
    @Column(name = "PASSWORD")
    protected String password;

    //todo remove?
    @Column(name = "NAME")
    protected String name;

    @Column(name = "FIRST_NAME")
    protected String firstName;

    @Column(name = "LAST_NAME")
    protected String lastName;

    @Column(name = "MIDDLE_NAME")
    protected String middleName;

    @Column(name = "EMAIL")
    protected String email;

    @Column(name = "LANGUAGE_", length = 20)
    protected String language;

    @Column(name = "TIME_ZONE")
    protected String timeZone;

    @Column(name = "ENABLED")
    protected Boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @InstanceName
    public String getCaption() {
        // todo rework when new instance name is ready
        String pattern =/* AppContext.getProperty("cuba.user.namePattern");
        if (StringUtils.isBlank(pattern)) {
            pattern =*/ "{1} [{0}]";
        /*}*/
        MessageFormat fmt = new MessageFormat(pattern);
        return StringUtils.trimToEmpty(fmt.format(new Object[]{
                StringUtils.trimToEmpty(username),
                StringUtils.trimToEmpty(name)
        }));
    }

    @Override
    public String getKey() {
        return id.toString();
    }

    @Override
    public String getDisplayName() {
        return name;
    }
}
