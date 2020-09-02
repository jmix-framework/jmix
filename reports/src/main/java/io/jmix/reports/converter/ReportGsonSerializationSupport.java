/*
 * Copyright (c) 2008-2019 Haulmont.
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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.haulmont.cuba.core.global.View;
import io.jmix.core.JmixEntity;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportTemplate;

import java.io.IOException;

public class ReportGsonSerializationSupport extends GsonSerializationSupport {
    public ReportGsonSerializationSupport() {
        exclusionPolicy = (objectClass, propertyName) ->
                Report.class.isAssignableFrom(objectClass) && "xml".equalsIgnoreCase(propertyName)
                        || ReportTemplate.class.isAssignableFrom(objectClass) && "content".equals(propertyName);
    }

    @Override
    protected void writeFields(JsonWriter out, JmixEntity entity) throws IOException {
        super.writeFields(out, entity);
        if (entity instanceof DataSet) {
            out.name("view");
            out.value(gsonBuilder.create().toJson(((DataSet) entity).getView()));
        }
    }

    @Override
    protected void readUnresolvedProperty(JmixEntity entity, String propertyName, JsonReader in) throws IOException {
        if (entity instanceof DataSet && "view".equals(propertyName)) {
            String viewDefinition = in.nextString();
            View view = gsonBuilder.create().fromJson(viewDefinition, View.class);
            ((DataSet) entity).setView(view);
        } else {
            super.readUnresolvedProperty(entity, propertyName, in);
        }
    }
}
