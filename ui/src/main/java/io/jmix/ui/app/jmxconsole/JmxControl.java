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

package io.jmix.ui.app.jmxconsole;

import io.jmix.ui.app.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanDomain;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanInfo;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanOperation;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface to provide JMX control functionality for local JMX interfaces
 */
public interface JmxControl {

    /**
     * Loads the list of managed bean infos
     *
     * @return the list with managed beans
     */
    List<ManagedBeanInfo> getManagedBeans();

    /**
     * Loads the managed bean by its ObjectName
     * @param beanObjectName exact ObjectName of the bean
     * @return found managed bean, null if no bean found
     */
    ManagedBeanInfo getManagedBean(String beanObjectName);

    /**
     * Loads attributes for managed bean descriptor
     *
     * @param info     managed bean descriptor
     */
    void loadAttributes(ManagedBeanInfo info);

    /**
     * Loads attribute by its name. Note that the reference from ManagedBeanInfo
     * to loaded ManagedBeanAttribute is not set.
     *
     * @param info     managed bean descriptor
     * @return loaded attribute, null if no attribute found.
     */
    @Nullable
    ManagedBeanAttribute loadAttribute(ManagedBeanInfo info, String attributeName);

    /**
     * Loads attribute value for managed bean attribute
     *
     * @param attribute attribute descriptor
     */
    void loadAttributeValue(ManagedBeanAttribute attribute);

    /**
     * Saves attribute value to JMX node
     *
     * @param attribute attribute descriptor
     */
    void saveAttributeValue(ManagedBeanAttribute attribute);

    /**
     * Searches for the bean operation by its name and argument types.
     * @param bean  managed bean descriptor
     * @param operationName operation exact name
     * @param argTypes operation argument types
     * @return Found operation descriptor, null if not found
     */
    ManagedBeanOperation getOperation(ManagedBeanInfo bean, String operationName, @Nullable String[] argTypes);

    /**
     * Invokes method of managed bean
     *
     * @param operation       operation descriptor
     * @param parameterValues array with parameter values
     * @return invocation result
     */
    Object invokeOperation(ManagedBeanOperation operation, Object[] parameterValues);

    /**
     * Loads list of managed bean domains
     *
     * @return the list of managed bean domains
     */
    List<ManagedBeanDomain> getDomains();
}