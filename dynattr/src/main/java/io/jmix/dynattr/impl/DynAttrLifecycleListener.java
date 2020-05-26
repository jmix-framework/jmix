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

import io.jmix.core.Entity;
import io.jmix.core.LoadContext;
import io.jmix.data.impl.OrmLifecycleListener;
import io.jmix.dynattr.DynAttrManager;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;

@Component(DynAttrLifecycleListener.NAME)
public class DynAttrLifecycleListener implements OrmLifecycleListener {
    public static final String NAME = "dynattr_DynAttrLifecycleListener";

    @Autowired
    protected DynAttrManager dynAttrManager;

    @Override
    public void onLoad(Collection<Entity> entities, LoadContext loadContext) {
        if (loadContext.isLoadDynamicAttributes()) {
            dynAttrManager.loadValues(entities, loadContext.getFetchPlan());
        }
    }

    @Override
    public void onSave(Collection<Entity> entities) {
        dynAttrManager.storeValues(entities);
    }
}
