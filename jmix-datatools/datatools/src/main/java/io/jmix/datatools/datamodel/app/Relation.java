package io.jmix.datatools.datamodel.app;

public record Relation(
    String referencedClass,
    String relationDescription
){}
