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

package io.jmix.graphql.schema.scalar;

import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import io.jmix.core.FileRef;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.datafetcher.GqlEntityValidationException;
import io.jmix.graphql.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.*;
import java.util.*;

import static io.jmix.graphql.schema.scalar.CustomScalars.*;

@Component("gqL_ScalarTypes")
public class ScalarTypes {

    protected GraphQLScalarType localTimeScalar;
    protected GraphQLScalarType timeScalar;
    protected GraphQLScalarType offsetTimeScalar;
    protected GraphQLScalarType fileRefScalar;

    final FileService fileService;

    @Autowired
    public ScalarTypes(DatatypeRegistry datatypeRegistry, FileService fileService) {
        this.localTimeScalar = new LocalTimeScalar(datatypeRegistry.get(LocalTime.class));
        this.timeScalar = new TimeScalar(datatypeRegistry.get(LocalTime.class), datatypeRegistry.get(Time.class));
        this.offsetTimeScalar = new OffsetTimeScalar(datatypeRegistry.get(OffsetTime.class));
        this.fileService = fileService;
        this.fileRefScalar = new FileRefScalar(fileService);
    }

    protected static GraphQLScalarType[] scalars = {
            Scalars.GraphQLInt,
            Scalars.GraphQLBigInteger,
            Scalars.GraphQLBoolean,
            Scalars.GraphQLByte,
            Scalars.GraphQLChar,
            Scalars.GraphQLFloat,
            Scalars.GraphQLShort,
            Scalars.GraphQLString,
            CustomScalars.GraphQLVoid,
            CustomScalars.GraphQLLocalDateTime,
            CustomScalars.GraphQLLocalDate,
            CustomScalars.GraphQLOffsetDateTime,
            CustomScalars.GraphQLDate,
            CustomScalars.GraphQLDateTime,
            GraphQLBigDecimal,
            GraphQLLong,
            CustomScalars.GraphQLUUID,
    };

    public String getScalarFieldTypeName(MetaProperty metaProperty) {
        Class<?> javaType = metaProperty.getRange().asDatatype().getJavaClass();

        // scalars from graphql-java

        if (String.class.isAssignableFrom(javaType))
            return Scalars.GraphQLString.getName();
        if (Character.class.isAssignableFrom(javaType))
            return Scalars.GraphQLChar.getName();
        if (Integer.class.isAssignableFrom(javaType) || int.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLInt.getName();
        }
        if (Short.class.isAssignableFrom(javaType) || short.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLShort.getName();
        }
        if (Float.class.isAssignableFrom(javaType) || float.class.isAssignableFrom(javaType)
                || Double.class.isAssignableFrom(javaType) || double.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLFloat.getName();
        }
        if (Boolean.class.isAssignableFrom(javaType) || boolean.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLBoolean.getName();
        }

        // more scalars added in jmix-graphql

        if (UUID.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLUUID.getName();
        }
        if (Long.class.isAssignableFrom(javaType) || long.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLong.getName();
        }
        if (BigDecimal.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLBigDecimal.getName();
        }
        if (FileRef.class.isAssignableFrom(javaType)) {
            return fileRefScalar.getName();
        }
        if (Date.class.isAssignableFrom(javaType)) {
            if (MetadataUtils.isDate(metaProperty)) {
                return CustomScalars.GraphQLDate.getName();
            }
            if (MetadataUtils.isTime(metaProperty)) {
                return timeScalar.getName();
            }
            if (MetadataUtils.isDateTime(metaProperty)) {
                return CustomScalars.GraphQLDateTime.getName();
            }
            throw new GqlEntityValidationException("Unsupported datatype mapping for date property " + metaProperty);
        }
        if (LocalDateTime.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLocalDateTime.getName();
        }
        if (LocalDate.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLocalDate.getName();
        }
        if (LocalTime.class.isAssignableFrom(javaType)) {
            return localTimeScalar.getName();
        }
        if (OffsetDateTime.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLOffsetDateTime.getName();
        }
        if (OffsetTime.class.isAssignableFrom(javaType)) {
            return offsetTimeScalar.getName();
        }

//        log.warn("getDatatypeFieldTypeName: can't resolve type for datatype meta property {} class {}", metaProperty, javaType);
        // todo a couple of classes are not supported now
        return "String";
//        throw new UnsupportedOperationException(String.format("Can't define field type name for datatype class %s", javaType));
    }

    public List<GraphQLScalarType> scalars() {
        List<GraphQLScalarType> scalarTypes = new ArrayList<>(Arrays.asList(scalars));
        scalarTypes.addAll(timeScalars());
        scalarTypes.add(fileRefScalar);
        return scalarTypes;
    }

    public List<GraphQLScalarType> timeScalars() {
        List<GraphQLScalarType> scalars = new ArrayList<>();
        scalars.add(localTimeScalar);
        scalars.add(timeScalar);
        scalars.add(offsetTimeScalar);
        return scalars;
    }

    public boolean isFileRefType(GraphQLScalarType scalar) {
        return fileRefScalar.getName().equals(scalar.getName());
    }

    public boolean isTimeType(GraphQLScalarType scalar) {
        return timeScalars().stream()
                .anyMatch(ts -> ts.getName().equals(scalar.getName()));
    }
}
