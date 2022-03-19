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

package io.jmix.rest.impl.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.ParameterizedDatatype;
import io.jmix.rest.exception.RestAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Component("rest_DatatypesControllerManager")
public class DatatypesControllerManager {

    private static final Logger log = LoggerFactory.getLogger(DatatypesControllerManager.class);

    @Autowired
    protected DatatypeRegistry datatypes;

    public String getDatatypesJson() {
        JsonArray jsonArray = new JsonArray();

        try {
            for (String id : datatypes.getIds()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", id);
                jsonObject.addProperty("name", id); // for backward compatibility

                Datatype datatype = datatypes.get(id);
                if (datatype instanceof ParameterizedDatatype) {
                    Map<String, Object> parameters = ((ParameterizedDatatype) datatype).getParameters();
                    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                        jsonObject.addProperty(entry.getKey(), entry.getValue().toString());
                    }
                }

                jsonArray.add(jsonObject);
            }
        } catch (Exception e) {
            log.error("Fail to get datatype settings", e);
            throw new RestAPIException("Fail to get datatype settings", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        return jsonArray.toString();
    }
}
