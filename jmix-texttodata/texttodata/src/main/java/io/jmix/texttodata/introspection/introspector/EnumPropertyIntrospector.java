package io.jmix.texttodata.introspection.introspector;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.texttodata.introspection.model.*;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("textdt_EnumPropertyIntrospector")
public class EnumPropertyIntrospector extends AbstractPropertyIntrospector {

    @Autowired
    protected Messages messages;

    @Override
    public boolean supports(MetaProperty property) {
        return property.getType() == MetaProperty.Type.ENUM;
    }

    @Nullable
    @Override
    public EntityPropertyDescriptor introspect(MetaProperty property) {
        if (!supports(property)) {
            return null;
        }

        Class<?> enumClass = property.getJavaType();
        Map<String, EnumValueDescriptor> enums = new HashMap<>();

        if (enumClass.isEnum()) {
            if (EnumClass.class.isAssignableFrom(enumClass)) {
                for (Object enumConstant : enumClass.getEnumConstants()) {
                    EnumClass<?> enumClassConstant = (EnumClass<?>) enumConstant;
                    String enumName = enumConstant.toString();
                    enums.put(enumName,
                            new EnumValueDescriptor(enumClassConstant.getId(), enumName,
                                    getLocalizedEnumValueNames(enumConstant)));
                }
            } else {
                for (Object enumConstant : enumClass.getEnumConstants()) {
                    String enumName = enumConstant.toString();
                    enums.put(enumName,
                            // TODO: pinyazhin, check storage type: ordinal or constant name
                            new EnumValueDescriptor(((Enum<?>) enumConstant).ordinal(), enumName, Collections.emptyList()));
                }
            }
            return new EnumPropertyDescriptor(property.getName(),
                    getPropertyLocalizedNames(property),
                    property.getJavaType().getSimpleName(),
                    getPropertyType(property),
                    null,
                    getComment(property),
                    // TODO: pinyazhin, how to get localized enum class name?
                    new EnumClassDescriptor(enumClass.getSimpleName(), Collections.emptyList(), enums)
                    );
        }
        return null;
    }

    protected List<String> getLocalizedEnumValueNames(Object enumConstant) {
        Collection<Locale> locales = messageTools.getAvailableLocalesMap().values();
        List<String> names = new ArrayList<>(locales.size());
        for (Locale locale : locales) {
            String localizedName = messages.getMessage((Enum<?>) enumConstant, locale);
            if (!enumConstant.toString().equals(localizedName) && !getEnumCaptionFallbackKey(enumConstant).equals(localizedName)) {
                names.add(localizedName);
            }
        }
        return names;
    }

    protected String getEnumCaptionFallbackKey(Object enumConstant) {
        Enum<?> enumValue = (Enum<?>) enumConstant;
        return enumValue.getDeclaringClass().getSimpleName() + "." + enumValue.name();
    }
}
