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

package io.jmix.reports.libintegration;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.util.converter.AbstractObjectToStringConverter;
import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.UUID;

public class JmixObjectToStringConverter extends AbstractObjectToStringConverter {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        if (paramValue == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return (String) paramValue;
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            //todo
            //return EntityLoadInfo.create((Entity) paramValue).toString();
            return paramValue.toString();
        } else {
            Datatype datatype = datatypeRegistry.find(parameterClass);
            if (datatype != null) {
                return datatype.format(paramValue);
            } else {
                return String.valueOf(paramValue);
            }
        }
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        if (paramValueStr == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return paramValueStr;
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            //todo
//            EntityLoadInfo entityLoadInfo = EntityLoadInfo.parse(paramValueStr);
//            if (entityLoadInfo != null) {
//                return dataManager.load(entityLoadInfo.getClass())
//                        .id(entityLoadInfo.getId())
//                        .fetchPlan(FetchPlan.BASE)
//                        .one();
//            } else {
                UUID id = UUID.fromString(paramValueStr);
                return dataManager.load(parameterClass)
                        .id(id)
                        .fetchPlan(FetchPlan.BASE)
                        .one();
//            }
        } else {
            Datatype datatype = datatypeRegistry.find(parameterClass);
            if (datatype != null) {
                try {
                    return datatype.parse(paramValueStr);
                } catch (ParseException e) {
                    throw new ReportingException(
                            String.format("Couldn't read value [%s] with datatype [%s].",
                                    paramValueStr, datatype));
                }
            } else {
                return convertFromStringUnresolved(parameterClass, paramValueStr);
            }
        }
    }
}
