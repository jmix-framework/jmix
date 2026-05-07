package io.jmix.texttodata.introspection.introspector;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.texttodata.introspection.model.EmbeddedPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component("textdt_EmbeddedPropertyIntrospector")
public class EmbeddedPropertyIntrospector extends AbstractPropertyIntrospector {

    @Override
    public boolean supports(MetaProperty property) {
        return property.getType() == MetaProperty.Type.EMBEDDED;
    }

    @Nullable
    @Override
    public EntityPropertyDescriptor introspect(MetaProperty property) {
        if (!supports(property)) {
            return null;
        }

        return new EmbeddedPropertyDescriptor(property.getName(),
                getPropertyLocalizedNames(property),
                property.getRange().asClass().getName(),
                getPropertyType(property),
                getIdentifier(property),
                getComment(property));
    }
}
