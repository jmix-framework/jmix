/*
 * Copyright 2025 Haulmont.
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

package index_definition;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicAttributesMockHelper {

    private static final Logger log = LoggerFactory.getLogger(DynamicAttributesMockHelper.class);

    public static AttributeDefinition createDynamicAttributeMockForDataType(
            String attributeCode,
            AttributeType attributeType,
            Datatype<?> datatype) {
        AttributeDefinition attributeDefinition = mock(AttributeDefinition.class);
        when(attributeDefinition.getCode()).thenReturn(attributeCode);
        when(attributeDefinition.getDataType()).thenReturn(attributeType);
        MetaProperty metaProperty = mock(MetaProperty.class);
        when(metaProperty.getName()).thenReturn("+" + attributeCode);
        when(attributeDefinition.getMetaProperty()).thenReturn(metaProperty);
        Range range = mock(Range.class);
        when(metaProperty.getRange()).thenReturn(range);
        when(range.isClass()).thenReturn(false);
        when(range.isDatatype()).thenReturn(true);
        when(range.asDatatype()).thenReturn((Datatype) datatype);
        when(metaProperty.getAnnotatedElement()).thenReturn(new FakeAnnotatedElement());
        return attributeDefinition;
    }

    public static AttributeDefinition createDynamicAttributeMockForReference(
            String attributeCode,
            MetaClass referenceMetaClass) {
        AttributeDefinition attributeDefinition = mock(AttributeDefinition.class);
        when(attributeDefinition.getCode()).thenReturn(attributeCode);
        when(attributeDefinition.getDataType()).thenReturn(AttributeType.ENTITY);
        MetaProperty metaProperty = mock(MetaProperty.class);
        when(metaProperty.getName()).thenReturn("+" + attributeCode);
        when(attributeDefinition.getMetaProperty()).thenReturn(metaProperty);
        Range range = mock(Range.class);
        when(metaProperty.getRange()).thenReturn(range);
        when(range.isClass()).thenReturn(true);
        when(range.isDatatype()).thenReturn(false);
        when(range.asClass()).thenReturn(referenceMetaClass);
        when(metaProperty.getAnnotatedElement()).thenReturn(new FakeAnnotatedElement());
        return attributeDefinition;
    }

    public static void addMocksToDynAttrMetadata(DynAttrMetadata dynAttrMetadataMock,
                                                 Class<?> entityClass,
                                                 AttributeDefinition... attributeDefinitions) {
        List<AttributeDefinition> definitionList = asList(attributeDefinitions);
        when(dynAttrMetadataMock.getAttributes(argThat(MetaClassMatcher.of(entityClass)))).thenReturn(definitionList);

        for(AttributeDefinition definition:attributeDefinitions){
            String code = definition.getCode();
            when(dynAttrMetadataMock.getAttributeByCode(argThat(MetaClassMatcher.of(entityClass)), eq(code))).thenReturn(Optional.of(definition));
            log.info("Mocked dynamic attribute '{}'", code);
        }
    }

    private static class MetaClassMatcher implements ArgumentMatcher<MetaClass> {

        static MetaClassMatcher of(Class<?> expectedJavaClass) {
            return new MetaClassMatcher(expectedJavaClass);
        }

        private final Class<?> expectedJavaClass;

        private MetaClassMatcher(Class<?> expectedJavaClass) {
            this.expectedJavaClass = expectedJavaClass;
        }

        @Override
        public boolean matches(MetaClass argument) {
            if (argument == null) {
                return false;
            }
            return argument.getJavaClass().equals(expectedJavaClass);
        }

        @Override
        public Class<?> type() {
            return ArgumentMatcher.super.type();
        }
    }

    private static class FakeAnnotatedElement implements AnnotatedElement, Serializable {

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return false;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }
}
