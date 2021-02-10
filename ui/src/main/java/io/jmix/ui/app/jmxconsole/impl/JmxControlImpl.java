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

package io.jmix.ui.app.jmxconsole.impl;

import io.jmix.core.Metadata;
import io.jmix.ui.app.jmxconsole.JmxAction;
import io.jmix.ui.app.jmxconsole.JmxControl;
import io.jmix.ui.app.jmxconsole.JmxControlException;
import io.jmix.ui.app.jmxconsole.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.management.*;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.rmi.UnmarshalException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("ui_JmxControl")
public class JmxControlImpl implements JmxControl {

    public static final String JMX_PORT_SYSTEM_PROPERTY = "com.sun.management.jmxremote.port";

    public static final String RMI_SERVER_HOSTNAME_SYSTEM_PROPERTY = "java.rmi.server.hostname";

    private final Logger log = LoggerFactory.getLogger(JmxControlImpl.class);

    @Autowired
    protected Metadata metadata;

    /**
     * Constant identifier for the role field in a JMX {@link Descriptor}.
     */
    protected static final String FIELD_ROLE = "role";

    /**
     * Constant identifier for the getter role field value in a JMX {@link Descriptor}.
     */
    protected static final String ROLE_GETTER = "getter";

    /**
     * Constant identifier for the setter role field value in a JMX {@link Descriptor}.
     */
    protected static final String ROLE_SETTER = "setter";


    @Override
    public List<ManagedBeanInfo> getManagedBeans() {
        //noinspection UnnecessaryLocalVariable
        List<ManagedBeanInfo> infos = withConnection((connection) -> {
            Set<ObjectName> names = connection.queryNames(null, null);
            List<ManagedBeanInfo> infoList = new ArrayList<>(names.size());
            for (ObjectName name : names) {
                MBeanInfo info;
                try {
                    info = connection.getMBeanInfo(name);
                } catch (UnmarshalException | InstanceNotFoundException e) {
                    // unable to use this bean, may be ClassNotFoundException
                    continue;
                }

                ManagedBeanInfo mbi = createManagedBeanInfo(name, info);
                loadOperations(mbi, info);
                infoList.add(mbi);
            }

            infoList.sort(new MBeanComparator());

            return infoList;
        });

        return infos;
    }

    @Override
    public ManagedBeanInfo getManagedBean(final String beanObjectName) {
        checkNotNullArgument(beanObjectName);

        //noinspection UnnecessaryLocalVariable
        ManagedBeanInfo info = withConnection((connection) -> {
            Set<ObjectName> names = connection.queryNames(new ObjectName(beanObjectName), null);
            ManagedBeanInfo mbi = null;
            if (CollectionUtils.isNotEmpty(names)) {
                ObjectName name = names.stream().findFirst().get();
                MBeanInfo info1 = connection.getMBeanInfo(name);
                mbi = createManagedBeanInfo(name, info1);
                loadOperations(mbi, info1);
            }

            return mbi;
        });

        return info;
    }

    protected ManagedBeanInfo createManagedBeanInfo(ObjectName name, MBeanInfo info) {
        ManagedBeanInfo mbi = metadata.create(ManagedBeanInfo.class);
        mbi.setClassName(info.getClassName());
        mbi.setDescription(info.getDescription());
        mbi.setObjectName(name.toString());
        mbi.setDomain(name.getDomain());
        mbi.setPropertyList(name.getKeyPropertyListString());

        return mbi;
    }

    @Override
    public void loadAttributes(final ManagedBeanInfo mbinfo) {
        checkNotNullArgument(mbinfo);

        withConnection((connection) -> {
            ObjectName name = new ObjectName(mbinfo.getObjectName());
            MBeanInfo info = connection.getMBeanInfo(name);
            MBeanAttributeInfo[] attributes = info.getAttributes();

            List<ManagedBeanAttribute> attrs = Arrays.stream(attributes)
                    .map(mBeanAttributeInfo -> createAttribute(connection, name, mBeanAttributeInfo, mbinfo))
                    .sorted(new AttributeComparator())
                    .collect(Collectors.toList());

            mbinfo.setAttributes(attrs);
            return null;
        });
    }

    @Override
    public ManagedBeanAttribute loadAttribute(final ManagedBeanInfo mbinfo, final String attributeName) {
        checkNotNullArgument(mbinfo);
        checkNotNullArgument(attributeName);

        //noinspection UnnecessaryLocalVariable
        ManagedBeanAttribute attribute = withConnection((connection) -> {
            ObjectName name = new ObjectName(mbinfo.getObjectName());
            MBeanInfo info = connection.getMBeanInfo(name);
            ManagedBeanAttribute res = null;
            Optional<MBeanAttributeInfo> foundAttributeInfo = Arrays.stream(info.getAttributes())
                    .filter(mBeanAttributeInfo -> mBeanAttributeInfo.getName().equals(attributeName))
                    .findFirst();
            if (foundAttributeInfo.isPresent()) {
                res = createAttribute(connection, name, foundAttributeInfo.get(), mbinfo);
            }

            return res;
        });
        return attribute;
    }

    protected ManagedBeanAttribute createAttribute(MBeanServerConnection connection, ObjectName name,
                                                   MBeanAttributeInfo attribute, ManagedBeanInfo mbinfo) {
        ManagedBeanAttribute mba = metadata.create(ManagedBeanAttribute.class);
        mba.setMbean(mbinfo);
        mba.setName(attribute.getName());
        mba.setDescription(attribute.getDescription());
        mba.setType(cleanType(attribute.getType()));
        mba.setReadable(attribute.isReadable());
        mba.setWriteable(attribute.isWritable());

        String mask = "";
        if (attribute.isReadable()) {
            mask += "R";
        }
        if (attribute.isWritable()) {
            mask += "W";
        }
        mba.setReadableWriteable(mask);

        if (mba.getReadable()) {
            try {
                Object value = connection.getAttribute(name, mba.getName());
                setSerializableValue(mba, value);
            } catch (Exception e) {
                log.error("Error getting attribute", e);
                mba.setValue(e.getMessage());
                mba.setWriteable(false);
            }
        }
        return mba;
    }

    @Override
    public void loadAttributeValue(final ManagedBeanAttribute attribute) {
        checkNotNullArgument(attribute);
        checkNotNullArgument(attribute.getMbean());

        withConnection((connection) -> {
            ObjectName name = new ObjectName(attribute.getMbean().getObjectName());

            Object value = null;
            if (attribute.getReadable()) {
                try {
                    value = connection.getAttribute(name, attribute.getName());
                } catch (Exception e) {
                    log.error("Error getting attribute", e);
                    value = e.getMessage();
                }
            }
            setSerializableValue(attribute, value);

            return null;
        });
    }

    @Override
    public ManagedBeanOperation getOperation(ManagedBeanInfo bean, String operationName, @Nullable String[] argTypes) {
        checkNotNullArgument(bean);
        checkNotNullArgument(operationName);

        ManagedBeanOperation res = null;

        Optional<ManagedBeanOperation> foundOperation = bean.getOperations().stream()
                .filter(op -> op.getName().equals(operationName) && equalAttributes(op.getParameters(), argTypes))
                .findFirst();
        if (foundOperation.isPresent()) {
            res = foundOperation.get();
        }
        return res;
    }


    @Override
    public void saveAttributeValue(final ManagedBeanAttribute attribute) {
        checkNotNullArgument(attribute);
        checkNotNullArgument(attribute.getMbean());

        withConnection((connection) -> {
            try {
                ObjectName name = new ObjectName(attribute.getMbean().getObjectName());
                Attribute a = new Attribute(attribute.getName(), attribute.getValue());

                log.info(String.format("Set value '%s' to attribute '%s' in '%s'",
                        a.getValue(), a.getName(), name.getCanonicalName()));

                connection.setAttribute(name, a);
            } catch (Exception e) {
                log.info(String.format("Unable to set value '%s' to attribute '%s' in '%s'",
                        attribute.getValue(), attribute.getName(), attribute.getMbean().getObjectName()), e);

                throw e;
            }

            return null;
        });
    }

    @Override
    public Object invokeOperation(final ManagedBeanOperation operation, final Object[] parameterValues) {
        checkNotNullArgument(operation);
        checkNotNullArgument(operation.getMbean());

        //noinspection UnnecessaryLocalVariable
        Object result = withConnection((connection) -> {
            try {
                ObjectName name = new ObjectName(operation.getMbean().getObjectName());

                String[] types = operation.getParameters()
                        .stream()
                        .map(ManagedBeanOperationParameter::getJavaType)
                        .toArray(String[]::new);

                log.debug(String.format("Invoke method '%s' from '%s'",
                        operation.getName(), name.getCanonicalName()));
                return connection.invoke(name, operation.getName(), parameterValues, types);
            } catch (Exception e) {
                log.warn(String.format("Error invoking method '%s' from '%s'",
                        operation.getName(), operation.getMbean().getObjectName()), e);
                throw e;
            }
        });

        return result;
    }

    @Override
    public List<ManagedBeanDomain> getDomains() {
        //noinspection UnnecessaryLocalVariable
        List<ManagedBeanDomain> domains = withConnection((connection) -> {
            String[] domainNames = connection.getDomains();

            return Arrays.stream(domainNames).map(this::createDomain)
                    .sorted(new DomainComparator())
                    .collect(Collectors.toList());
        });

        return domains;
    }

    protected boolean equalAttributes(List<ManagedBeanOperationParameter> args, String[] argTypes) {
        if (ArrayUtils.isEmpty(argTypes)) {
            return true;
        }
        if (args.size() == argTypes.length) {
            boolean notEqualAttrFound = IntStream.range(0, args.size())
                    .filter(i -> !args.get(i).getType().equals(argTypes[i]))
                    .findFirst()
                    .isPresent();
            return !notEqualAttrFound;
        }
        return false;
    }

    protected void loadOperations(ManagedBeanInfo mbean, MBeanInfo info) {
        MBeanOperationInfo[] operations = info.getOperations();

        List<ManagedBeanOperation> opList = Arrays.stream(operations)
                .filter(mBeanOperationInfo -> !isGetterOrSetter(mBeanOperationInfo))
                .map(mBeanOperationInfo -> createOperation(mbean, mBeanOperationInfo))
                .sorted(new OperationComparator())
                .collect(Collectors.toList());

        mbean.setOperations(opList);
    }

    protected boolean isGetterOrSetter(MBeanOperationInfo operation) {
        Descriptor descriptor = operation.getDescriptor();
        String role = (String) descriptor.getFieldValue(FIELD_ROLE);
        return ROLE_GETTER.equals(role) || ROLE_SETTER.equals(role);
    }

    protected ManagedBeanOperationParameter createOperationParameter(ManagedBeanOperation o, int index, MBeanParameterInfo pinfo) {
        ManagedBeanOperationParameter p = metadata.create(ManagedBeanOperationParameter.class);
        p.setName(pinfo.getName());
        p.setType(cleanType(pinfo.getType()));
        p.setJavaType(pinfo.getType());
        p.setDescription(pinfo.getDescription());
        p.setOperation(o);

        // fix name if it is not set
        if (StringUtils.isEmpty(p.getName()) || p.getName().equals(p.getType())) {
            p.setName("arg" + index);
        }
        return p;
    }

    protected ManagedBeanOperation createOperation(ManagedBeanInfo mbean, MBeanOperationInfo operation) {
        ManagedBeanOperation o = metadata.create(ManagedBeanOperation.class);
        o.setName(operation.getName());
        o.setDescription(operation.getDescription());
        o.setMbean(mbean);
        o.setReturnType(cleanType(operation.getReturnType()));

        List<ManagedBeanOperationParameter> paramList = new ArrayList<>();
        MBeanParameterInfo[] mBeanParameterInfos = operation.getSignature();
        if (mBeanParameterInfos != null) {
            paramList = IntStream.range(0, mBeanParameterInfos.length)
                    .mapToObj(value -> createOperationParameter(o, value, mBeanParameterInfos[value]))
                    .collect(Collectors.toList());
        }
        o.setParameters(paramList);

        return o;
    }

    protected ManagedBeanDomain createDomain(String d) {
        ManagedBeanDomain mbd = metadata.create(ManagedBeanDomain.class);
        mbd.setName(d);
        return mbd;
    }

    protected String cleanType(String type) {
        if (type != null && type.startsWith("[L") && type.endsWith(";")) {
            return type.substring(2, type.length() - 1) + "[]";
        }
        return type;
    }

    protected void setSerializableValue(ManagedBeanAttribute mba, Object value) {
        if (value instanceof Serializable && !(value instanceof Proxy)) {
            mba.setValue(value);
        } else if (value != null) {
            mba.setValue(value.toString());
        }

    }

    /**
     * Sorts domains alphabetically by name
     */
    protected static class DomainComparator implements Comparator<ManagedBeanDomain> {
        @Override
        public int compare(ManagedBeanDomain mbd1, ManagedBeanDomain mbd2) {
            return mbd1 != null && mbd1.getName() != null
                    ? mbd1.getName().compareTo(mbd2.getName())
                    : (mbd2 != null && mbd2.getName() != null ? 1 : 0);
        }
    }

    /**
     * Sorts mbeans alphabetically by name
     */
    protected static class MBeanComparator implements Comparator<ManagedBeanInfo> {
        @Override
        public int compare(ManagedBeanInfo mbd1, ManagedBeanInfo mbd2) {
            return mbd1 != null && mbd1.getPropertyList() != null
                    ? mbd1.getPropertyList().compareTo(mbd2.getPropertyList())
                    : (mbd2 != null && mbd2.getPropertyList() != null ? 1 : 0);
        }
    }

    /**
     * Sorts attributes alphabetically by name
     */
    protected static class AttributeComparator implements Comparator<ManagedBeanAttribute> {
        @Override
        public int compare(ManagedBeanAttribute mbd1, ManagedBeanAttribute mbd2) {
            return mbd1 != null && mbd1.getName() != null
                    ? mbd1.getName().compareTo(mbd2.getName())
                    : (mbd2 != null && mbd2.getName() != null ? 1 : 0);
        }
    }

    /**
     * Sorts operations alphabetically by name
     */
    protected static class OperationComparator implements Comparator<ManagedBeanOperation> {
        @Override
        public int compare(ManagedBeanOperation o1, ManagedBeanOperation o2) {
            return o1 != null && o1.getName() != null
                    ? o1.getName().compareTo(o2.getName())
                    : (o2 != null && o2.getName() != null ? 1 : 0);
        }
    }

    protected static <T> T withConnection(JmxAction<T> action) {
        try {
            return action.perform(ManagementFactory.getPlatformMBeanServer());
        } catch (Exception e) {
            throw new JmxControlException(e);
        }
    }
}