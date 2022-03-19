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

package io.jmix.reports.converter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import io.jmix.core.FetchPlan;
import io.jmix.reports.entity.*;

import java.util.*;

public class XStreamConverter {
    protected XStream createXStream() {
        XStream xStream = new ReportsXStream(Collections.singletonList(SerializableConverter.class)) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }

                    @Override
                    public Class realClass(String elementName) {
                        return super.realClass(elementName);
                    }
                };
            }
        };
        xStream.registerConverter(new CollectionConverter(xStream.getMapper()) {
            @Override
            public boolean canConvert(Class type) {
                return ArrayList.class.isAssignableFrom(type) ||
                        HashSet.class.isAssignableFrom(type) ||
                        LinkedList.class.isAssignableFrom(type) ||
                        LinkedHashSet.class.isAssignableFrom(type);

            }
        }, XStream.PRIORITY_VERY_HIGH);

        xStream.registerConverter(new DateConverter() {
            @Override
            public boolean canConvert(Class type) {
                return Date.class.isAssignableFrom(type);
            }
        });

        xStream.alias("report", Report.class);
        xStream.alias("band", BandDefinition.class);
        xStream.alias("dataSet", DataSet.class);
        xStream.alias("parameter", ReportInputParameter.class);
        xStream.alias("template", ReportTemplate.class);
        xStream.alias("screen", ReportScreen.class);
        xStream.alias("format", ReportValueFormat.class);
        xStream.addDefaultImplementation(LinkedHashMap.class, Map.class);
        xStream.aliasSystemAttribute(null, "class");

        xStream.omitField(Report.class, "xml");
        xStream.omitField(Report.class, "deleteTs");
        xStream.omitField(Report.class, "deletedBy");
        xStream.omitField(Report.class, "detached");
        xStream.omitField(ReportTemplate.class, "content");
        xStream.omitField(ReportTemplate.class, "defaultFlag");
        xStream.omitField(ReportTemplate.class, "templateFileDescriptor");
        xStream.omitField(ReportTemplate.class, "deleteTs");
        xStream.omitField(ReportTemplate.class, "deletedBy");
        xStream.omitField(ReportTemplate.class, "detached");
        xStream.omitField(ReportInputParameter.class, "localeName");
        xStream.omitField(ReportGroup.class, "detached");
        xStream.omitField(FetchPlan.class, "includeSystemProperties");
        xStream.omitField(ReportRole.class, "detached");
        xStream.omitField(ReportScreen.class, "detached");

        xStream.aliasField("customFlag", ReportTemplate.class, "custom");
        xStream.aliasField("customClass", ReportTemplate.class, "customDefinition");
        xStream.aliasField("uuid", BandDefinition.class, "id");
        xStream.aliasField("uuid", DataSet.class, "id");
        xStream.aliasField("uuid", ReportInputParameter.class, "id");
        xStream.aliasField("uuid", ReportScreen.class, "id");
        xStream.aliasField("definedBy", ReportTemplate.class, "customDefinedBy");
        xStream.aliasField("uuid", ReportValueFormat.class, "id");

        return xStream;
    }

    public String convertToString(Report report) {
        XStream xStream = createXStream();
        //noinspection UnnecessaryLocalVariable
        String xml = xStream.toXML(report);
        return xml;
    }

    public Report convertToReport(String xml) {
        XStream xStream = createXStream();
        return (Report) xStream.fromXML(xml);
    }
}
