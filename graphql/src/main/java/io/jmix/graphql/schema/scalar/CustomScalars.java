package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;

import java.text.SimpleDateFormat;

public class CustomScalars {

    public static final SimpleDateFormat SERIALIZATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static GraphQLScalarType GraphQLDate = new DateScalar();
    public static GraphQLScalarType GraphQLLocalDateTime = new LocalDateTimeScalar();

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
}
