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

package io.jmix.reportsui.screen.template.edit.generator;

import io.jmix.core.Metadata;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.reports.entity.pivottable.PivotTableDescription;
import io.jmix.reports.entity.pivottable.PivotTableProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component("report_RandomPivotTableDataGenerator")
public class RandomPivotTableDataGenerator {

    @Autowired
    protected Metadata metadata;

    public List<KeyValueEntity> generate(PivotTableDescription description, int size) {
        List<KeyValueEntity> result = new ArrayList<>(size);
        List<String> aggregationProperties = description.getAggregationProperties();
        for (int i = 0; i < size; i++) {
            KeyValueEntity entity = metadata.create(KeyValueEntity.class);
            for (PivotTableProperty property : description.getProperties()) {
                entity.setValue(property.getName(),
                        generatePropertyValue(property.getName(), aggregationProperties.contains(property.getName()), size));
            }
            result.add(entity);
        }
        return result;
    }

    protected Object generatePropertyValue(String name, boolean aggregation, int size) {
        if (aggregation) {
            return ThreadLocalRandom.current().nextDouble(100);
        } else {
            return name + "#" + (ThreadLocalRandom.current().nextInt(size * 3) + 1);
        }
    }
}
