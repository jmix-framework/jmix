/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components.filter.descriptor;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.DynamicAttributesCondition;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.apache.commons.lang3.RandomStringUtils;

@JmixEntity(name = "sec$DynamicAttributesConditionCreator")
@SystemLevel
public class DynamicAttributesConditionCreator extends AbstractConditionDescriptor {

    protected String propertyPath;

    public DynamicAttributesConditionCreator(String filterComponentName, io.jmix.core.metamodel.model.MetaClass  metaClass,
                                             String propertyPath, String entityAlias) {
        super(RandomStringUtils.randomAlphabetic(10), filterComponentName, metaClass, entityAlias);
        this.propertyPath = propertyPath;
        Messages messages = AppBeans.get(Messages.class);
        locCaption = messages.getMainMessage("filter.dynamicAttributeConditionCreator");
        showImmediately = true;
    }

    @Override
    public AbstractCondition createCondition() {
        return new DynamicAttributesCondition(this, entityAlias, propertyPath);
    }

    @Override
    public Class getJavaClass() {
        return null;
    }

    @Override
    public String getEntityParamWhere() {
        return null;
    }

    @Override
    public String getEntityParamView() {
        return null;
    }
}
