/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.mapping.impl

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.search.index.mapping.DisplayedNameDescriptor
import io.jmix.search.index.mapping.FieldConfiguration
import io.jmix.search.index.mapping.IndexMappingConfiguration
import io.jmix.search.index.mapping.MappingFieldDescriptor
import io.jmix.search.utils.Constants
import spock.lang.Specification


class IndexMappingConfigurationSerializerTest extends Specification {

    public static final String JSON_TEXT = "{\"properties\":{\"customer\":{\"properties\":{\"status\":{\"type\":\"text\"},\"lastName\":{\"type\":\"text\"}}},\"number\":{\"type\":\"text\"},\"product\":{\"type\":\"text\"},\"_instance_name\":{\"type\":\"text\"}}}";

    def "fields merging"() {

        given:
        IndexMappingConfigurationSerializer serializer = new IndexMappingConfigurationSerializer()
        DisplayedNameDescriptor displayedNameDescriptorMock = getDisplayedNameDescriptor()
        IndexMappingConfiguration configuration = new IndexMappingConfiguration(Mock(MetaClass.class), getFields(), displayedNameDescriptorMock)

        when:
        ObjectNode objectNode = serializer.mergeFields(configuration)

        then:
        JSON_TEXT == objectNode.toString()
    }

    private HashMap<String, MappingFieldDescriptor> getFields() {
        HashMap<String, MappingFieldDescriptor> map = new HashMap<>();
        map.put("customer.status", createMappingFieldDescriptor("customer.status"));
        map.put("number", createMappingFieldDescriptor("number"));
        map.put("product", createMappingFieldDescriptor("product"));
        map.put("customer.lastName", createMappingFieldDescriptor("customer.lastName"));
        return map;
    }

    private MappingFieldDescriptor createMappingFieldDescriptor(String fieldName) {
        MappingFieldDescriptor descriptor = Mock(MappingFieldDescriptor.class)
        descriptor.getFieldConfiguration() >> createFieldConfiguration()
        descriptor.getIndexPropertyFullName() >> fieldName
        return descriptor;
    }

    private DisplayedNameDescriptor getDisplayedNameDescriptor() {
        DisplayedNameDescriptor displayedNameDescriptorMock = Mock(DisplayedNameDescriptor.class);
        displayedNameDescriptorMock.getIndexPropertyFullName() >> Constants.INSTANCE_NAME_FIELD;
        FieldConfiguration configuration = createFieldConfiguration();
        displayedNameDescriptorMock.getFieldConfiguration() >> configuration
        return displayedNameDescriptorMock;
    }

    private static FieldConfiguration createFieldConfiguration() {
        ObjectNode fieldConfig = JsonNodeFactory.instance.objectNode();
        fieldConfig.put("type", "text");
        return FieldConfiguration.create(fieldConfig);
    }

}
