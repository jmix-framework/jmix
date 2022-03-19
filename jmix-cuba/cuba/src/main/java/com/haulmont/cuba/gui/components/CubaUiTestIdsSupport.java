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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.data.meta.DatasourceDataUnit;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.impl.UiTestIdsSupport;

import javax.annotation.Nullable;

public class CubaUiTestIdsSupport extends UiTestIdsSupport {

    @Nullable
    @Override
    public String getInferredTestId(DataUnit dataUnit, String suffix) {
        if (dataUnit instanceof DatasourceDataUnit) {
            DatasourceDataUnit dsDataUnit = (DatasourceDataUnit) dataUnit;

            MetaClass entityMetaClass = dsDataUnit.getDatasource().getMetaClass();

            return entityMetaClass.getName() + suffix;
        }

        return super.getInferredTestId(dataUnit, suffix);
    }
}
