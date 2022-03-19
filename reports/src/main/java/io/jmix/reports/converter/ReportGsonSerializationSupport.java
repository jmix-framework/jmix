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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportTemplate;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;

public class ReportGsonSerializationSupport extends GsonSerializationSupport {

    public ReportGsonSerializationSupport(BeanFactory beanFactory) {
        super(beanFactory);
        exclusionPolicy = (objectClass, propertyName) ->
                Report.class.isAssignableFrom(objectClass) && "xml".equalsIgnoreCase(propertyName)
                        || ReportTemplate.class.isAssignableFrom(objectClass) && "content".equals(propertyName);
    }

    @Override
    protected void writeFields(JsonWriter out, Entity entity) throws IOException {
        super.writeFields(out, entity);
        if (entity instanceof DataSet) {
            out.name("fetchPlan");
            out.value(gsonBuilder.create().toJson(((DataSet) entity).getFetchPlan()));
        }
    }

    @Override
    protected void readUnresolvedProperty(Entity entity, String propertyName, JsonReader in) throws IOException {
        if (entity instanceof DataSet && "fetchPlan".equals(propertyName)) {
            String fetchPlanDefinition = in.nextString();
            FetchPlan fetchPlan = gsonBuilder.create().fromJson(fetchPlanDefinition, FetchPlan.class);
            ((DataSet) entity).setFetchPlan(fetchPlan);
        } else {
            super.readUnresolvedProperty(entity, propertyName, in);
        }
    }
}
