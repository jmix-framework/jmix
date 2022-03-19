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

package io.jmix.graphql.service;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.Id;
import java.util.UUID;

@Component("gql_IdentifierService")
public class IdentifierService {

    @Autowired
    MetadataTools metadataTools;

    private MetaProperty getIdProperty(MetaClass metaClass) {
        return metadataTools.getPrimaryKeyProperty(metaClass);
    }

    public Object parse(String id, MetaClass metaClass) {
        MetaProperty property = getIdProperty(metaClass);

        if (property.getJavaType().equals(UUID.class)) {
            return UUID.fromString(id);
        }
        if (property.getJavaType().equals(Integer.class)) {
            return Integer.parseInt(id);
        }
        if (property.getJavaType().equals(Long.class)) {
            return Long.parseLong(id);
        }
        return id;
    }

}
