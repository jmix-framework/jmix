/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.data.ValueSource;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public final class UiTestIds {

    private UiTestIds() {
    }

    @Nullable
    public static String getInferredTestId(ValueSource<?> valueSource) {
        if (valueSource instanceof ContainerValueSource) {
            ContainerValueSource dcValueSource = (ContainerValueSource) valueSource;

            return StringUtils.join(dcValueSource.getMetaPropertyPath().getPropertyNames(), "_");
        }

        return null;
    }

    @Nullable
    public static String getInferredTestId(DataUnit dataUnit, String suffix) {
        if (dataUnit instanceof ContainerDataUnit) {
            ContainerDataUnit dcDataUnit = (ContainerDataUnit) dataUnit;

            MetaClass entityMetaClass = dcDataUnit.getEntityMetaClass();

            return entityMetaClass.getName() + suffix;
        } /* TODO: legacy-ui
          else if (dataUnit instanceof DatasourceDataUnit) {
            DatasourceDataUnit dsDataUnit = (DatasourceDataUnit) dataUnit;

            MetaClass entityMetaClass = dsDataUnit.getDatasource().getMetaClass();

            return entityMetaClass.getName() + suffix;
        }*/

        return null;
    }
}
