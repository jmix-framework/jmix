package io.jmix.aitools.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class RelationPropertyDescriptor extends AbstractEntityPropertyDescriptor {

    protected String mappedBy;

    protected String targetEntityName;

    protected List<String> targetEntityLocalizedNames;

    protected Boolean optionalRelation;

    protected String cardinality;

    public RelationPropertyDescriptor(String name,
                                      List<String> localizedNames,
                                      String javaType,
                                      String propertyType,
                                      @Nullable Boolean identifier,
                                      Boolean persistent,
                                      Boolean mandatory,
                                      @Nullable String comment,
                                      String mappedBy,
                                      String targetEntityName,
                                      List<String> targetEntityLocalizedNames,
                                      @Nullable Boolean optionalRelation,
                                      String cardinality) {
        super(name, localizedNames, javaType, propertyType, identifier, persistent, mandatory, comment);
        this.mappedBy = mappedBy;
        this.targetEntityName = targetEntityName;
        this.targetEntityLocalizedNames = targetEntityLocalizedNames;
        this.optionalRelation = optionalRelation;
        this.cardinality = cardinality;
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public String getTargetEntityName() {
        return targetEntityName;
    }

    public List<String> getTargetEntityLocalizedNames() {
        return targetEntityLocalizedNames;
    }

    @Nullable
    public Boolean getOptionalRelation() {
        return optionalRelation;
    }

    public String getCardinality() {
        return cardinality;
    }

    @Override
    protected String fieldsToString() {
        return super.fieldsToString() +
                ", mappedBy='" + mappedBy + "'" +
                ", targetEntityName='" + targetEntityName + "'" +
                ", targetEntityLocalizedNames=" + targetEntityLocalizedNames +
                ", optionalRelation=" + optionalRelation +
                ", cardinality='" + cardinality + "'";
    }
}
