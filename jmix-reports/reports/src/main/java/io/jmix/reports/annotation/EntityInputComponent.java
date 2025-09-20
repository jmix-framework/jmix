package io.jmix.reports.annotation;

/**
 * Determines which component is used to the entity input parameter in UI.
 *
 * @see EntityParameterDef
 * @see io.jmix.reports.entity.ParameterType#ENTITY
 */
public enum EntityInputComponent {

    /**
     * EntityComboBox component is used. Options are loaded with a JPQL query.
     */
    OPTION_LIST,

    /**
     * EntityPicker component is used, which opens a lookup view.
     */
    LOOKUP_VIEW
}
