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
import com.haulmont.cuba.gui.components.filter.ConditionParamBuilder;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.CustomCondition;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Condition descriptor is used for creating new custom condition
 */
@JmixEntity(name = "sec$CustomConditionCreator")
@SystemLevel
public class CustomConditionCreator extends AbstractConditionDescriptor {

    public CustomConditionCreator(String filterComponentName, MetaClass metaClass,
                                  String entityAlias) {
        super(RandomStringUtils.randomAlphabetic(10), filterComponentName, metaClass, entityAlias);

        Messages messages = AppBeans.get(Messages.class);
        this.locCaption = messages.getMainMessage("filter.customCondition.new");
        showImmediately = true;
    }

    @Override
    public AbstractCondition createCondition() {
        CustomCondition customCondition = new CustomCondition(this, null, null, entityAlias, false);

        // default editor - text
        customCondition.setJavaClass(String.class);
        Param param = AppBeans.get(ConditionParamBuilder.class).createParam(customCondition);
        customCondition.setParam(param);

        return customCondition;
    }

    @Override
    public String getTreeCaption() {
        Messages messages = AppBeans.get(Messages.class);
        return messages.getMainMessage("filter.customConditionCreator");
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
