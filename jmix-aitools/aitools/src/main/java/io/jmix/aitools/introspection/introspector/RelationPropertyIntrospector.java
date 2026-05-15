package io.jmix.aitools.introspection.introspector;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.aitools.introspection.model.EntityPropertyDescriptor;
import io.jmix.aitools.introspection.model.RelationPropertyDescriptor;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("textdt_RelationPropertyIntrospector")
public class RelationPropertyIntrospector extends AbstractPropertyIntrospector {

    @Override
    public boolean supports(MetaProperty property) {
        MetaProperty.Type type = property.getType();

        return (type == MetaProperty.Type.ASSOCIATION || type == MetaProperty.Type.COMPOSITION)
                && property.getRange().getCardinality() != Range.Cardinality.NONE;
    }

    @Nullable
    @Override
    public EntityPropertyDescriptor introspect(MetaProperty property) {
        if (!supports(property)) {
            return null;
        }

        Range range = property.getRange();

        String targetEntityName = range.asClass().getName();
        List<String> targetEntityLocalizedNames = getEntityLocalizedNames(range.asClass());

        return new RelationPropertyDescriptor(property.getName(),
                getPropertyLocalizedNames(property),
                range.asClass().getName(),
                getPropertyType(property),
                null,
                getPersistent(property),
                getMandatory(property),
                getComment(property),
                getMappedByValue(property),
                targetEntityName,
                targetEntityLocalizedNames,
                isOptionalRelation(property) ? null : false,
                range.getCardinality().name());
    }


    protected boolean isOptionalRelation(MetaProperty property) {
        if (property.isMandatory()) {
            return false;
        }

        ManyToOne manyToOne = property.getAnnotatedElement().getAnnotation(ManyToOne.class);
        if (manyToOne != null) {
            return manyToOne.optional();
        }

        OneToOne oneToOne = property.getAnnotatedElement().getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            return oneToOne.optional();
        }

        return true;
    }

    @Nullable
    protected String getMappedByValue(MetaProperty property) {
        OneToMany oneToMany = property.getAnnotatedElement().getAnnotation(OneToMany.class);
        if (oneToMany != null && !oneToMany.mappedBy().isBlank()) {
            return oneToMany.mappedBy();
        }

        OneToOne oneToOne = property.getAnnotatedElement().getAnnotation(OneToOne.class);
        if (oneToOne != null && !oneToOne.mappedBy().isBlank()) {
            return oneToOne.mappedBy();
        }

        ManyToMany manyToMany = property.getAnnotatedElement().getAnnotation(ManyToMany.class);
        if (manyToMany != null && !manyToMany.mappedBy().isBlank()) {
            return manyToMany.mappedBy();
        }

        Composition composition = property.getAnnotatedElement().getAnnotation(Composition.class);
        if (composition != null && !composition.inverse().isBlank()) {
            return composition.inverse();
        }

        return null;
    }
}
