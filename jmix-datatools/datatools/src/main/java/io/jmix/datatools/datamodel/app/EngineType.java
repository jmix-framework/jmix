package io.jmix.datatools.datamodel.app;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Enum describes the engine type manually selected by the user in the application.properties file
 */
public enum EngineType implements EnumClass<String> {

    PLANTUML("PlantUML"),
    MERMAID("Mermaid");

    private final String id;

    EngineType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EngineType fromId(String id) {
        for (EngineType at : EngineType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}