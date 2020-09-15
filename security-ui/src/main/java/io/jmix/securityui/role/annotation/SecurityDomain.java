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

package io.jmix.securityui.role.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that annotated class belongs to a specific "security domain". "Security domain" is a group of screens,
 * entities, entity attributes, menu items and other application elements.
 * <p>
 * For example the "Order" domain may contain the app_Order entity, all entity attributes, standard order browser and
 * editor screens and a menu item that opens the order browser screen.
 * <p>
 * {@link SecurityDomain} annotation can be put on entity class and screen controller class.
 * <p>
 * Domains are used in the role editor ({@link io.jmix.securityui.screen.role.RoleModelEdit}) for resource policies
 * grouping.
 *
 * @see io.jmix.securityui.model.ResourcePolicyDomainResolver
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityDomain {

    String name();
}
