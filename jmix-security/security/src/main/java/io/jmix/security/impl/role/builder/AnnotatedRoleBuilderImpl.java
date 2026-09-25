/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.impl.role.builder;

import com.google.common.collect.Sets;
import io.jmix.core.ClassManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.impl.MessageSourceConfiguration;
import io.jmix.security.impl.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.impl.role.builder.extractor.RowLevelPolicyExtractor;
import io.jmix.security.model.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NullMarked
@Component("sec_AnnotatedRoleBuilder")
public class AnnotatedRoleBuilderImpl implements AnnotatedRoleBuilder {

    private final Collection<ResourcePolicyExtractor> resourcePolicyExtractors;
    private final Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors;
    private final ClassManager classManager;

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    // The basenames of @MessageSourceBasenames are registered when this configuration is initialized, so the texts of
    // roles are read only after that: an earlier read would cache the bundles without them.
    @SuppressWarnings("unused")
    @Autowired
    protected MessageSourceConfiguration messageSourceConfiguration;

    @Autowired
    public AnnotatedRoleBuilderImpl(Collection<ResourcePolicyExtractor> resourcePolicyExtractors,
                                    Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors,
                                    ClassManager classManager) {
        this.resourcePolicyExtractors = resourcePolicyExtractors;
        this.rowLevelPolicyExtractors = rowLevelPolicyExtractors;
        this.classManager = classManager;
    }

    @Override
    public ResourceRole createResourceRole(String className) {
        Class<?> roleClass = loadClass(className);

        io.jmix.security.role.annotation.ResourceRole roleAnnotation =
                roleClass.getAnnotation(io.jmix.security.role.annotation.ResourceRole.class);

        ResourceRole role = new ResourceRole();
        initBaseParameters(role, roleClass, roleAnnotation.name(), roleAnnotation.code(), roleAnnotation.description());
        role.setScopes(Sets.newHashSet(roleAnnotation.scope()));
        role.setResourcePolicies(extractResourcePolicies(roleClass));

        return role;
    }

    @Override
    public RowLevelRole createRowLevelRole(String className) {
        Class<?> roleClass = loadClass(className);

        io.jmix.security.role.annotation.RowLevelRole roleAnnotation =
                roleClass.getAnnotation(io.jmix.security.role.annotation.RowLevelRole.class);

        RowLevelRole role = new RowLevelRole();
        initBaseParameters(role, roleClass, roleAnnotation.name(), roleAnnotation.code(), roleAnnotation.description());
        role.setRowLevelPolicies(extractRowLevelPolicies(roleClass));

        return role;
    }

    protected Class<?> loadClass(String className) {
        return Objects.requireNonNull(classManager.findClass(className), "Class " + className + " is not found");
    }

    /**
     * Sets the base parameters of a role. A name or a description declared as a {@code msg://} reference gets the
     * message in the default locale, and the role keeps the key of the message.
     */
    protected void initBaseParameters(BaseRole role, Class<?> roleClass, String name, String code, String description) {
        String nameMessageKey = name.startsWith(MessageTools.MARK) ? extractMessageKey(name, roleClass) : null;
        String descriptionMessageKey =
                description.startsWith(MessageTools.MARK) ? extractMessageKey(description, roleClass) : null;

        initBaseParameters(role,
                nameMessageKey == null ? name : getDefaultName(nameMessageKey),
                code,
                descriptionMessageKey == null ? description : getDefaultDescription(descriptionMessageKey));

        role.setNameMessageKey(nameMessageKey);
        role.setDescriptionMessageKey(descriptionMessageKey);
    }

    protected void initBaseParameters(BaseRole role, String name, String code, String description) {
        role.setName(name);
        role.setCode(code);
        role.setDescription(description);
        role.setSource(RoleSource.ANNOTATED_CLASS);
    }

    /**
     * @return the message of the key in the default locale, or the key if the message is missing or blank, so that
     * the role never goes without a name
     */
    protected String getDefaultName(String messageKey) {
        String message = messages.getMessage(messageKey, messageTools.getDefaultLocale());
        return message.isBlank() ? messageKey : message;
    }

    /**
     * @return the message of the key in the default locale, or the key if the message is missing
     */
    protected String getDefaultDescription(String messageKey) {
        return messages.getMessage(messageKey, messageTools.getDefaultLocale());
    }

    /**
     * Extracts the key of the message a {@code msg://} reference points to. The reference takes one of the three
     * forms a view descriptor accepts: {@code msg://key} belongs to the package of the role class,
     * {@code msg://group/key} keeps its group and {@code msg:///key} belongs to the main message group. Any other
     * reference with a slash in it, such as {@code msg://a/b/c}, is taken as it is: what follows {@code msg://}
     * becomes the key.
     */
    protected String extractMessageKey(String reference, Class<?> roleClass) {
        String path = reference.substring(MessageTools.MARK.length());
        if (path.startsWith("/")) {
            return path.substring(1);
        }

        return path.contains("/") ? path : roleClass.getPackageName() + "/" + path;
    }

    protected Collection<ResourcePolicy> extractResourcePolicies(Class<?> roleClass) {
        List<ResourcePolicy> policies = new ArrayList<>();
        for (Method method : roleClass.getMethods()) {
            for (ResourcePolicyExtractor policyExtractor : resourcePolicyExtractors) {
                policies.addAll(policyExtractor.extractResourcePolicies(method));
            }
        }
        return policies;
    }

    protected Collection<RowLevelPolicy> extractRowLevelPolicies(Class<?> roleClass) {
        List<RowLevelPolicy> policies = new ArrayList<>();
        for (Method method : roleClass.getMethods()) {
            for (RowLevelPolicyExtractor policyExtractor : rowLevelPolicyExtractors) {
                policies.addAll(policyExtractor.extractRowLevelPolicies(method));
            }
        }
        return policies;
    }
}
