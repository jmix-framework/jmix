/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.UserRepository;
import io.jmix.restds.impl.RestInvoker;
import io.jmix.security.authentication.AcceptsGrantedAuthorities;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base implementation of {@link UserRepository} that loads users from the REST DataStore.
 * The type of entity representing users is specified in the {@link #getUserClass()} method.
 *
 * @param <T> type of entity representing users
 */
public abstract class AbstractRestUserRepository<T extends UserDetails> implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(AbstractRestUserRepository.class);
    private T systemUser;
    private T anonymousUser;

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;
    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;
    @Autowired
    protected RowLevelRoleRepository rowLevelRoleRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    @Override
    public UserDetails getSystemUser() {
        return systemUser;
    }

    @Override
    public UserDetails getAnonymousUser() {
        return anonymousUser;
    }

    @Override
    public List<? extends UserDetails> getByUsernameLike(String substring) {
        return dataManager.load(getUserClass())
                .condition(PropertyCondition.contains("username", substring))
                .list();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String dataStoreName = metadata.getClass(getUserClass()).getStore().getName();
        RestInvoker restInvoker = applicationContext.getBean(RestInvoker.class, dataStoreName);

        T user = loadUser(restInvoker, username);
        if (user instanceof AcceptsGrantedAuthorities) {
            ((AcceptsGrantedAuthorities) user).setAuthorities(createAuthorities(restInvoker));
        }
        return user;
    }

    private T loadUser(RestInvoker restInvoker, String username) {
        String json = restInvoker.userInfo();

        try {
            JsonNode rootNode = objectMapper.readTree(json);

            JsonNode usernameNode = rootNode.get("username");
            if (!username.equals(usernameNode.asText()))
                throw new IllegalStateException("userInfo endpoint returned different user: " + usernameNode.asText());

            T user = metadata.create(getUserClass());

            JsonNode attributesNode = rootNode.get("attributes");
            if (attributesNode != null) {
                MetaClass userMetaClass = metadata.getClass(getUserClass());
                for (MetaProperty metaProperty : userMetaClass.getProperties()) {
                    String propertyName = metaProperty.getName();
                    if (metaProperty.getRange().isDatatype() && attributesNode.has(propertyName)) {
                        String text = attributesNode.path(propertyName).asText(null);
                        try {
                            Object value = metaProperty.getRange().asDatatype().parse(text);
                            EntityValues.setValue(user, propertyName, value);
                        } catch (ParseException e) {
                            log.warn("Unable to parse {} value: {}", propertyName, text);
                        }
                    }
                }
            } else {
                log.warn("userInfo endpoint didn't return attributes");
            }
            return user;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing userInfo JSON", e);
        }
    }

    protected Collection<? extends GrantedAuthority> createAuthorities(RestInvoker restInvoker) {
        String json = restInvoker.permissions();

        List<GrantedAuthority> authorities = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode authoritiesNode = rootNode.get("authorities");
            for (JsonNode authorityNode : authoritiesNode) {
                String authorityString = authorityNode.asText();

                String resourceRolePrefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
                String rowLevelRolePrefix = roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix();
                if (authorityString.startsWith(resourceRolePrefix)) {
                    if (resourceRoleRepository.findRoleByCode(authorityString.substring(resourceRolePrefix.length())) != null) {
                        authorities.add(createAuthority(authorityString));
                    }
                } else if (authorityString.startsWith(rowLevelRolePrefix)) {
                    if (rowLevelRoleRepository.findRoleByCode(authorityString.substring(rowLevelRolePrefix.length())) != null) {
                        authorities.add(createAuthority(authorityString));
                    }
                } else {
                    log.warn("Unknown role prefix: {}", authorityString);
                }
            }
            return authorities;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing permissions JSON", e);
        }
    }

    protected GrantedAuthority createAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }

    /**
     * Returns the class of an entity representing users in the application.
     */
    protected abstract Class<T> getUserClass();

    /**
     * Creates the built-in 'system' user.
     */
    protected T createSystemUser() {
        T systemUser = metadata.create(getUserClass());
        EntityValues.setValue(systemUser, "username", "system");
        initSystemUser(systemUser);
        return systemUser;
    }

    /**
     * Initializes the built-in 'system' user.
     * Override in the application to grant authorities or initialize attributes.
     */
    protected void initSystemUser(T systemUser) {
    }

    /**
     * Creates the built-in 'anonymous' user.
     */
    protected T createAnonymousUser() {
        T anonymousUser = metadata.create(getUserClass());
        EntityValues.setValue(anonymousUser, "username", "anonymous");
        initAnonymousUser(anonymousUser);
        return anonymousUser;
    }

    /**
     * Initializes the built-in 'anonymous' user.
     * Override in the application to grant authorities or initialize attributes.
     */
    protected void initAnonymousUser(T anonymousUser) {
    }

    protected GrantedAuthoritiesBuilder getGrantedAuthoritiesBuilder() {
        return new GrantedAuthoritiesBuilder();
    }

    /**
     * Helps create authorities from roles.
     */
    public class GrantedAuthoritiesBuilder {

        private List<GrantedAuthority> authorities = new ArrayList<>();

        /**
         * Adds a resource role by its code.
         */
        public GrantedAuthoritiesBuilder addResourceRole(String code) {
            GrantedAuthority authority = roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(code);
            authorities.add(authority);
            return this;
        }

        /**
         * Adds a row-level role by its code.
         */
        public GrantedAuthoritiesBuilder addRowLevelRole(String code) {
            GrantedAuthority authority = roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(code);
            authorities.add(authority);
            return this;
        }

        /**
         * Builds a collection of authorities.
         */
        public Collection<GrantedAuthority> build() {
            return authorities;
        }
    }
}
