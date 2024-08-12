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

package io.jmix.searchelasticsearch.index.impl;

import co.elastic.clients.elasticsearch._types.mapping.ObjectProperty;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.mapping.DisplayedNameDescriptor;
import io.jmix.search.index.mapping.FieldConfiguration;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElasticsearchPutMappingServiceTest {

    public static final String INDEX_NAME = "indexName";

    @Test
    void buildRequest() {
        ElasticsearchPutMappingService builder = new ElasticsearchPutMappingService();
        PutMappingRequest request = builder.buildRequest(createMappingConfiguration(), INDEX_NAME, new JacksonJsonpMapper());
        assertEquals(1, request.index().size());
        assertEquals(INDEX_NAME, request.index().get(0));
        assertEquals(4, request.properties().size());
        assertEquals(2, getObjectProperty(request, "customer").properties().size());
    }

    private ObjectProperty getObjectProperty(PutMappingRequest request, String propertyName) {
        return request.properties().get(propertyName).object();
    }


    //TODO move to search module
    private IndexMappingConfiguration createMappingConfiguration() {
        DisplayedNameDescriptor displayedNameDescriptorMock = getDisplayedNameDescriptor();
        return new IndexMappingConfiguration(mock(MetaClass.class), getFields(), displayedNameDescriptorMock);
    }

    private static HashMap<String, MappingFieldDescriptor> getFields() {
        HashMap<String, MappingFieldDescriptor> map = new HashMap<>();
        map.put("customer.status", createMappingFieldDescriptor("customer.status"));
        map.put("number", createMappingFieldDescriptor("number"));
        map.put("product", createMappingFieldDescriptor("product"));
        map.put("customer.lastName", createMappingFieldDescriptor("customer.lastName"));
        return map;
    }

    private static MappingFieldDescriptor createMappingFieldDescriptor(String fieldName) {
        MappingFieldDescriptor descriptor = mock(MappingFieldDescriptor.class);
        when(descriptor.getFieldConfiguration()).thenReturn(createFieldConfiguration());
        when(descriptor.getIndexPropertyFullName()).thenReturn(fieldName);
        return descriptor;
    }

    private static DisplayedNameDescriptor getDisplayedNameDescriptor() {
        DisplayedNameDescriptor displayedNameDescriptorMock = mock(DisplayedNameDescriptor.class);
        when(displayedNameDescriptorMock.getIndexPropertyFullName()).thenCallRealMethod();
        FieldConfiguration configuration = createFieldConfiguration();
        when(displayedNameDescriptorMock.getFieldConfiguration()).thenReturn(configuration);
        return displayedNameDescriptorMock;
    }

    private static FieldConfiguration createFieldConfiguration() {
        ObjectNode fieldConfig = JsonNodeFactory.instance.objectNode();
        fieldConfig.put("type", "text");
        return FieldConfiguration.create(fieldConfig);
    }
}