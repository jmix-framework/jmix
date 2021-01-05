package io.jmix.graphql.schema.scalar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UUIDCoercing extends BaseScalarCoercing {

    static final Logger log = LoggerFactory.getLogger(UUIDCoercing.class);

    @Override
    public Object serialize(Object input) {
        return (input instanceof UUID) ? input : null;
    }

    @Override
    public Object parseValue(Object input) {
        if (input instanceof String) {
            try {
                return UUID.fromString((String) input);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to parse UUID from input: " + input, e);
                return null;
            }
        }
        return null;
    }

}


