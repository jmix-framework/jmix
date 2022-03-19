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

package com.haulmont.cuba.web.gui.components.table;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.options.DatasourceOptions;
import io.jmix.core.AccessManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.UiComponentsGenerator;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.impl.AbstractTable;
import io.jmix.ui.component.table.TableFieldFactoryImpl;

import javax.annotation.Nullable;

public class CubaTableFieldFactoryImpl<E> extends TableFieldFactoryImpl<E> {

    public CubaTableFieldFactoryImpl(AbstractTable<?, E> webTable,
                                     AccessManager accessManager, MetadataTools metadataTools,
                                     UiComponentsGenerator uiComponentsGenerator) {
        super(webTable, accessManager, metadataTools, uiComponentsGenerator);
    }


    @Nullable
    @Override
    protected Options getOptions(EntityValueSource valueSource, String property) {
        MetaClass metaClass = valueSource.getEntityMetaClass();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);
        Table.Column columnConf = webTable.getColumnsInternal().get(metaPropertyPath);

        CollectionDatasource ds = findOptionsDatasource(columnConf, property);
        if (ds != null) {
            return new DatasourceOptions(ds);
        }

        return super.getOptions(valueSource, property);
    }

    @Nullable
    protected CollectionDatasource findOptionsDatasource(Table.Column columnConf, String propertyId) {
        String optDsName = columnConf.getXmlDescriptor() != null ?
                columnConf.getXmlDescriptor().attributeValue("optionsDatasource") : "";

        if (Strings.isNullOrEmpty(optDsName)) {
            return null;
        } else {
            if (getCubaTable().getDatasource() == null) {
                throw new IllegalStateException("Table datasource is null");
            }

            DsContext dsContext = getCubaTable().getDatasource().getDsContext();
            CollectionDatasource ds = (CollectionDatasource) dsContext.get(optDsName);
            if (ds == null) {
                throw new IllegalStateException(
                        String.format("Options datasource for table column '%s' not found: %s", propertyId, optDsName));
            }

            return ds;
        }
    }

    protected com.haulmont.cuba.gui.components.Table getCubaTable() {
        return ((com.haulmont.cuba.gui.components.Table) webTable);
    }
}
