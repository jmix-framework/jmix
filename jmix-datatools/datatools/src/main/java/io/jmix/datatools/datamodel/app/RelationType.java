package io.jmix.datatools.datamodel.app;

import io.jmix.core.common.util.Preconditions;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum describes the types of relationships needed to build an entity model and relationships between entities
 */
public enum RelationType {

    MANY_TO_ONE,
    ONE_TO_MANY,
    ONE_TO_ONE,
    MANY_TO_MANY;

    private final static Map<RelationType, RelationType> relationsTypesWithReverse = new HashMap<>() {
        {
            put(MANY_TO_ONE, ONE_TO_MANY);
            put(ONE_TO_MANY, MANY_TO_ONE);
            put(MANY_TO_MANY, MANY_TO_MANY);
            put(ONE_TO_ONE, ONE_TO_ONE);
        }
    };

    public static RelationType getReverseRelation(@NonNull RelationType relationType) {
        Preconditions.checkNotNullArgument(relationType);
        return relationsTypesWithReverse.get(relationType);
    }
}
