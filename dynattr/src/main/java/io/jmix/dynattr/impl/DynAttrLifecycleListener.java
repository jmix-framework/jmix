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

package io.jmix.dynattr.impl;

import io.jmix.core.JmixEntity;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.data.impl.OrmLifecycleListener;
import io.jmix.dynattr.DynAttrManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component(DynAttrLifecycleListener.NAME)
public class DynAttrLifecycleListener implements OrmLifecycleListener {
    public static final String NAME = "dynattr_DynAttrLifecycleListener";

    @Autowired
    protected DynAttrManager dynAttrManager;

    @Override
    public void onLoad(Collection<JmixEntity> entities, LoadContext loadContext) {
        if (loadContext.isLoadDynamicAttributes()) {
            //noinspection unchecked
            dynAttrManager.loadValues(entities, loadContext.getFetchPlan(), loadContext.getAccessConstraints());
        }
    }

    @Override
    public void onSave(Collection<JmixEntity> entities, SaveContext saveContext) {
        dynAttrManager.storeValues(entities, saveContext.getAccessConstraints());
    }
}
