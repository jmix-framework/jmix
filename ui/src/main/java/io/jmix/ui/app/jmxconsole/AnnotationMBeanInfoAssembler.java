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

import io.jmix.core.annotation.RunAsync;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedOperation;

import javax.annotation.Nonnull;
import javax.management.Descriptor;
import java.lang.reflect.Method;

/**
 * This assembler extends {@link MetadataMBeanInfoAssembler}.
 * Use to process custom annotations (e.g. @{@link RunAsync}) in MBeans.
 */
public class AnnotationMBeanInfoAssembler extends MetadataMBeanInfoAssembler {
    protected static final String FIELD_RUN_ASYNC = "runAsync";
    protected static final String FIELD_TIMEOUT = "timeout";

    /* Extracts annotation information from jmx interface */
    private final JmxAttributeSource attributeSource;

    public AnnotationMBeanInfoAssembler(JmxAttributeSource attributeSource) {
        super(attributeSource);
        this.attributeSource = attributeSource;
    }

    @Override
    protected String getOperationDescription(Method method, String beanKey) {
        String res = super.getOperationDescription(method, beanKey);
        if (StringUtils.equals(res, method.getName())) {
            return "";
        }
        return res;
    }

    /**
     * Adds descriptor fields from the {@code ManagedAttribute} attribute to the attribute descriptor. Specifically,
     * adds the {@code currencyTimeLimit} descriptor field if it is present in the metadata.
     */
    @Override
    protected void populateOperationDescriptor(@Nonnull Descriptor desc, Method method, String beanKey) {
        ManagedOperation mo = this.attributeSource.getManagedOperation(method);
        if (mo != null) {
            applyRunAsync(desc, method);
            applyCurrencyTimeLimit(desc, mo.getCurrencyTimeLimit());
        }
    }

    /**
     * Adds fields to the operation descriptor in case of operation should be executed asynchronously if
     * <code>operation</code> was annotated by {@link RunAsync}.
     *
     * @param desc      operation descriptor
     * @param operation operation
     */
    protected void applyRunAsync(Descriptor desc, Method operation) {
        RunAsync runAsync = operation.getAnnotation(RunAsync.class);
        if (runAsync == null) {
            return;
        }

        desc.setField(FIELD_RUN_ASYNC, true);
        desc.setField(FIELD_TIMEOUT, runAsync.timeout());
    }
}