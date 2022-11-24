/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.libintegration;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.structure.BandData;
import io.jmix.core.*;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.reports.app.EntityMap;
import io.jmix.reports.entity.JmixTableData;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;
import io.jmix.reports.entity.table.TemplateTableDescription;
import io.jmix.reports.exception.ReportingException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static io.jmix.reports.app.EntityMap.INSTANCE_NAME_KEY;
import static io.jmix.reports.entity.wizard.ReportRegion.HEADER_BAND_PREFIX;

@Component("report_JmixTableFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixTableFormatter extends AbstractFormatter {

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected StandardSerialization standardSerialization;

    protected JmixTableFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    @Override
    public void renderDocument() {
        JmixTableData dto = transformData(rootBand);
        byte[] serializedData = standardSerialization.serialize(dto);
        try {
            IOUtils.write(serializedData, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while rendering chart", e);
        }
    }

    protected JmixTableData transformData(BandData rootBand) {
        TemplateTableDescription templateTableDescription = ((ReportTemplate) reportTemplate).getTemplateTableDescription();

        Map<String, List<KeyValueEntity>> transformedData = new LinkedHashMap<>();
        Map<String, Set<JmixTableData.ColumnInfo>> headerMap = new HashMap<>();
        Map<String, List<BandData>> childrenBands = rootBand.getChildrenBands();

        if (templateTableDescription.getTemplateTableBands().size() > 0) {
            return getTableData(templateTableDescription, transformedData, headerMap, childrenBands);
        } else {
            return getTableData(transformedData, headerMap, childrenBands);
        }
    }

    private JmixTableData getTableData(Map<String, List<KeyValueEntity>> transformedData,
                                       Map<String, Set<JmixTableData.ColumnInfo>> headerMap,
                                       Map<String, List<BandData>> childrenBands) {
        childrenBands.forEach((bandName, bandDataList) -> {
            if (bandName.startsWith(HEADER_BAND_PREFIX)) {
                return;
            }
            List<KeyValueEntity> entities = new ArrayList<>();
            Set<JmixTableData.ColumnInfo> headers = new HashSet<>();
            Set<String> emptyHeaders = new LinkedHashSet<>();

            bandDataList.forEach(bandData -> {
                Map<String, Object> data = bandData.getData();
                final Entity instance;
                final String pkName;
                final boolean pkInFetchPlan;

                if (data instanceof EntityMap) {
                    instance = ((EntityMap) data).getInstance();
                    pkName = metadataTools.getPrimaryKeyName(metadata.getClass(instance));
                    FetchPlan fetchPlan = ((EntityMap) data).getFetchPlan();
                    pkInFetchPlan = fetchPlan != null && pkName != null && fetchPlan.containsProperty(pkName);
                } else {
                    instance = null;
                    pkName = null;
                    pkInFetchPlan = false;
                }

                KeyValueEntity entityRow = new KeyValueEntity();

                data.forEach((name, value) -> {
                    if (INSTANCE_NAME_KEY.equals(name)) {
                        return;
                    }
                    if (checkAddHeader(pkName, pkInFetchPlan, name)) {
                        if (instance != null) {
                            name = messageTools.getPropertyCaption(metadata.getClass(instance), name);
                        }

                        checkInstanceNameLoaded(value);
                        String transformationKey = transformationKey(name);
                        if (isFormat(bandName, name)) {
                            String formattedValue = getFormattedValue(bandData.getName(), name, value);
                            entityRow.setValue(transformationKey, formattedValue);
                        } else {
                            entityRow.setValue(transformationKey, value);
                        }
                    }
                });

                if (headers.isEmpty() || headers.size() < data.size()) {
                    data.forEach((name, value) -> {
                        if (INSTANCE_NAME_KEY.equals(name)) {
                            return;
                        }
                        if (checkAddHeader(pkName, pkInFetchPlan, name)) {
                            if (instance != null) {
                                name = messageTools.getPropertyCaption(metadata.getClass(instance), name);
                            }

                            if (name != null && value != null) {
                                Class valueClass = getColumnClass(bandData.getName(), name, value);
                                headers.add(new JmixTableData.ColumnInfo(transformationKey(name), valueClass, name));
                            }
                            if (name != null && value == null) {
                                emptyHeaders.add(transformationKey(name));
                            }
                        }

                    });
                }
                entities.add(entityRow);
            });

            emptyHeaders.forEach(header -> {
                if (!containsHeader(headers, header))
                    headers.add(new JmixTableData.ColumnInfo(header, String.class, header));
            });

            headers.removeIf(header -> containsLowerCaseDuplicate(header, headers));

            transformedData.put(bandName, entities);
            headerMap.put(bandName, headers);
        });

        return new JmixTableData(transformedData, headerMap);
    }

    protected JmixTableData getTableData(TemplateTableDescription templateTableDescription,
                                         Map<String, List<KeyValueEntity>> transformedData,
                                         Map<String, Set<JmixTableData.ColumnInfo>> headerMap,
                                         Map<String, List<BandData>> childrenBands) {
        for (TemplateTableBand band : templateTableDescription.getTemplateTableBands()) {
            String bandName = band.getBandName();

            if (bandName.startsWith(HEADER_BAND_PREFIX)) {
                break;
            }

            List<BandData> bandDataList = childrenBands.get(bandName);
            if (bandDataList == null) {
                throw new ReportingException(String.format("Report template has an unknown band [%s]", bandName));
            }

            List<KeyValueEntity> entities = new ArrayList<>();
            Set<JmixTableData.ColumnInfo> headers = new LinkedHashSet<>();

            bandDataList.forEach(bandData -> {
                Map<String, Object> data = bandData.getData();
                final String pkName;
                final boolean pkInFetchPlan;

                if (data instanceof EntityMap) {
                    Entity instance = ((EntityMap) data).getInstance();
                    pkName = metadataTools.getPrimaryKeyName(metadata.getClass(instance));
                    FetchPlan fetchPlan = ((EntityMap) data).getFetchPlan();
                    pkInFetchPlan = fetchPlan != null && pkName != null && fetchPlan.containsProperty(pkName);
                } else {
                    pkName = null;
                    pkInFetchPlan = false;
                }

                KeyValueEntity entityRow = new KeyValueEntity();

                for (TemplateTableColumn column : band.getColumns()) {
                    String key = column.getDataKey();
                    Object value = data.get(key);

                    if (INSTANCE_NAME_KEY.equals(key)) {
                        return;
                    }

                    if (checkAddHeader(pkName, pkInFetchPlan, key)) {
                        checkInstanceNameLoaded(value);

                        String transformationKey = transformationKey(key);
                        if (isFormat(bandName, key)) {
                            String formattedValue = getFormattedValue(bandName, key, value);
                            entityRow.setValue(transformationKey, formattedValue);
                        } else {
                            entityRow.setValue(transformationKey, value);
                        }
                    }
                }

                if (headers.isEmpty() || headers.size() < data.size()) {
                    for (TemplateTableColumn column : band.getColumns()) {
                        String key = column.getDataKey();
                        Object value = data.get(key);

                        if (INSTANCE_NAME_KEY.equals(key)) {
                            return;
                        }
                        if (checkAddHeader(pkName, pkInFetchPlan, key)) {

                            String transformationKey = transformationKey(key);
                            if (value != null) {
                                Class valueClass = getColumnClass(bandName, key, value);
                                headers.add(new JmixTableData.ColumnInfo(transformationKey, valueClass, column.getCaption(), column.getPosition()));
                            } else {
                                headers.add(new JmixTableData.ColumnInfo(transformationKey, String.class, column.getCaption(), column.getPosition()));
                            }
                        }
                    }
                }
                entities.add(entityRow);
            });

            headers.removeIf(header -> containsLowerCaseDuplicate(header, headers));

            transformedData.put(bandName, entities);
            headerMap.put(bandName, headers);
        }
        return new JmixTableData(transformedData, headerMap);
    }

    private String getFormattedValue(String bandName, String name, Object value) {
        return formatValue(value, name, generateFullParameterName(bandName, name));
    }

    private Class getColumnClass(String bandName, String parameterName, Object value) {
        return isFormat(bandName, parameterName) ? String.class : value.getClass();
    }

    private boolean isFormat(String bandName, String parameterName) {
        String format = getFormatString(parameterName, generateFullParameterName(bandName, parameterName));
        return format != null;
    }

    private String generateFullParameterName(String bandName, String parameterName) {
        return bandName + "." + parameterName;
    }

    private boolean checkAddHeader(@Nullable String pkName, boolean pkInFetchPlan, String name) {
        return pkName == null || !pkName.equals(name) || pkInFetchPlan;
    }

    protected boolean containsLowerCaseDuplicate(JmixTableData.ColumnInfo columnInfo, Set<JmixTableData.ColumnInfo> headers) {
        if (!columnInfo.getKey().equals(columnInfo.getKey().toUpperCase()))
            return false;

        for (JmixTableData.ColumnInfo header : headers) {
            if (!columnInfo.equals(header)
                    && header.getKey().toUpperCase().equals(columnInfo.getKey())
                    && header.getCaption().toUpperCase().equals(columnInfo.getCaption()))
                return true;
        }
        return false;
    }

    protected void checkInstanceNameLoaded(Object value) {
        if (!(value instanceof Entity || value instanceof EntityMap))
            return;

        if (value instanceof EntityMap)
            value = ((EntityMap) value).getInstance();

        try {
            metadataTools.getInstanceName((Entity) value);
        } catch (RuntimeException e) {
            throw new ReportFormattingException("Cannot fetch instance name for entity " + value.getClass()
                    + ". Please add all attributes used at instance name to report configuration.", e);
        }
    }

    protected boolean containsHeader(Set<JmixTableData.ColumnInfo> headers, String header) {
        for (JmixTableData.ColumnInfo columnInfo : headers) {
            if (columnInfo.getKey().equals(header))
                return true;
        }
        return false;
    }

    private String transformationKey(String key) {
        return key.replace('.', '-');
    }
}
