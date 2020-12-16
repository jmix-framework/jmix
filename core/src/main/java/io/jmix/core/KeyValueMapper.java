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

package io.jmix.core;

import io.jmix.core.entity.KeyValueEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("core_KeyValueMapper")
public class KeyValueMapper {

    public List<KeyValueEntity> mapValues(List<Object> values,
                                          String idProperty,
                                          List<String> properties,
                                          List<Integer> deniedProperties) {
        return values.stream()
                .map(obj -> mapToKeyValueEntity(obj, idProperty, properties, deniedProperties))
                .collect(Collectors.toList());
    }

    protected KeyValueEntity mapToKeyValueEntity(Object object,
                                                 String idProperty,
                                                 List<String> properties,
                                                 List<Integer> deniedProperties) {
        //noinspection JmixIncorrectCreateEntity
        KeyValueEntity entity = new KeyValueEntity();
        entity.setIdName(idProperty);

        if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            for (int i = 0; i < properties.size(); i++) {
                String key = properties.get(i);
                if (array.length > i) {
                    if (deniedProperties.contains(i)) {
                        entity.setValue(key, null);
                    } else {
                        entity.setValue(key, array[i]);
                    }
                }
            }
        } else if (!properties.isEmpty()) {
            if (!deniedProperties.isEmpty()) {
                entity.setValue(properties.get(0), null);
            } else {
                entity.setValue(properties.get(0), object);
            }
        }

        return entity;
    }
}
