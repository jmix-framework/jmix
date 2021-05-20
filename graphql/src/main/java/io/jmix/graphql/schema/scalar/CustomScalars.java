package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BigDecimalCoercing;
import io.jmix.graphql.schema.scalar.coercing.LongCoercing;
import io.jmix.graphql.schema.scalar.coercing.UUIDCoercing;
import io.jmix.graphql.schema.scalar.coercing.VoidCoercing;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomScalars {

    public static final SimpleDateFormat SERIALIZATION_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final SimpleDateFormat SERIALIZATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SERIALIZATION_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static GraphQLScalarType GraphQLDate = new DateScalar();
    public static GraphQLScalarType GraphQLTime = new TimeScalar();
    public static GraphQLScalarType GraphQLDateTime = new DateTimeScalar();
    public static GraphQLScalarType GraphQLLocalDateTime = new LocalDateTimeScalar();
    public static GraphQLScalarType GraphQLLocalDate = new LocalDateScalar();
    public static GraphQLScalarType GraphQLLocalTime = new LocalTimeScalar();
    public static GraphQLScalarType GraphQLOffsetDateTime = new OffsetDateTimeScalar();
    public static GraphQLScalarType GraphQLOffsetTime = new OffsetTimeScalar();

    public static GraphQLScalarType GraphQLLong = GraphQLScalarType.newScalar()
            .name("Long")
            .coercing(new LongCoercing()).build();

    public static GraphQLScalarType GraphQLBigDecimal = GraphQLScalarType.newScalar()
            .name("BigDecimal")
            .coercing(new BigDecimalCoercing()).build();

    public static GraphQLScalarType GraphQLUUID = GraphQLScalarType.newScalar()
            .name("UUID")
            .coercing(new UUIDCoercing()).build();


    public static GraphQLScalarType GraphQLVoid = GraphQLScalarType.newScalar()
            .name("Void").coercing(new VoidCoercing()).build();

    public static Map<Class, GraphQLScalarType> scalarsByClass() {
        Map<Class, GraphQLScalarType> scalars = new HashMap<>();
        scalars.put(Date.class, GraphQLDate);
        // todo two scalars for date
        // scalars.put(Date.class, GraphQLDateTime);
        scalars.put(Time.class, GraphQLTime);
        scalars.put(LocalDateTime.class, GraphQLLocalDateTime);
        scalars.put(LocalDate.class, GraphQLLocalDate);
        scalars.put(LocalTime.class, GraphQLLocalTime);
        scalars.put(OffsetDateTime.class, GraphQLOffsetDateTime);
        scalars.put(OffsetTime.class, GraphQLOffsetTime);
        scalars.put(Long.class, GraphQLLong);
        scalars.put(BigDecimal.class, GraphQLBigDecimal);
        scalars.put(UUID.class, GraphQLUUID);
        return scalars;
    }
}
