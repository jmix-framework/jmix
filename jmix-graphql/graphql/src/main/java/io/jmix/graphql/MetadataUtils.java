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

package io.jmix.graphql;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

@Component("gql_MetadataUtils")
public class MetadataUtils {

    @Autowired
    MetadataTools metadataTools;

    public static final String DATATYPE_ID_DATE = "date";
    public static final String DATATYPE_ID_TIME = "time";
    public static final String DATATYPE_ID_DATETIME = "dateTime";

    public static boolean isDateTime(MetaProperty metaProperty) {
        return DATATYPE_ID_DATETIME.equals(getDatatypeId(metaProperty));
    }

    public static boolean isTime(MetaProperty metaProperty) {
        return DATATYPE_ID_TIME.equals(getDatatypeId(metaProperty));
    }

    public static boolean isDate(MetaProperty metaProperty) {
        return DATATYPE_ID_DATE.equals(getDatatypeId(metaProperty));
    }

    public static String getDatatypeId(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getId();
    }

    public List<MetaClass> allSupportedMetaClasses() {
        return metadataTools.getAllJpaEntityMetaClasses()
                // todo need to be fixed later - ReferenceToEntity is not persistent but returned in 'metadataTools.getAllPersistentMetaClasses'
                .stream()
                .filter(metaClass -> !metaClass.getJavaClass().getSimpleName().equals("ReferenceToEntity"))
                .collect(Collectors.toList());
    }

    /**
     * @return enums java classes used in generic model
     */
    public List<Class<?>> allEnumJavaClasses() {
        return allSupportedMetaClasses().stream()
                .flatMap(metaClass -> metaClass.getProperties().stream())
                .filter(metaProperty -> metaProperty.getType() == MetaProperty.Type.ENUM)
                .map(MetaProperty::getJavaType)
                .distinct()
                .collect(Collectors.toList());
    }

    public static Enum<?>[] getEnumValues(Class<?> javaType) {
        Enum<?>[] enumValues;
        try {
            enumValues = (Enum<?>[]) javaType.getDeclaredMethod("values").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Can't build enum type definition for java type " + javaType.getName(), e);
        }
        return enumValues;
    }


}
