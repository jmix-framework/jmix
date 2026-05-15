package io.jmix.aitools.introspection.introspector;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.aitools.introspection.model.DatatypePropertyDescriptor;
import io.jmix.aitools.introspection.model.EntityPropertyDescriptor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component("aitols_DatatypePropertyIntrospector")
public class DatatypePropertyIntrospector extends AbstractPropertyIntrospector {

    @Override
    public boolean supports(MetaProperty property) {
        return property.getRange().isDatatype();
    }

    @Nullable
    @Override
    public EntityPropertyDescriptor introspect(MetaProperty property) {
        if (!supports(property)) {
            return null;
        }

        return new DatatypePropertyDescriptor(property.getName(),
                getPropertyLocalizedNames(property),
                property.getJavaType().getSimpleName(),
                property.getType().name().toLowerCase(),
                getIdentifier(property),
                getPersistent(property),
                getMandatory(property),
                getComment(property));
    }
}
