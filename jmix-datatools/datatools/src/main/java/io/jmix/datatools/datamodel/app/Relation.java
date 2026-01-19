package io.jmix.datatools.datamodel.app;

/**
 * Record describes one relationship between two attributes of two entities. It is necessary for building entity model
 * @param referencedClass an {@link io.jmix.core.Entity} model id that represents a specific attribute of an entity
 * referenced by a class field of a current {@link io.jmix.datatools.datamodel.entity.EntityModel}
 * @param relationDescription a string relation description for a specific {@link EngineType}
 */
public record Relation(
    String dataStore,
    String referencedClass,
    String relationDescription
){}
