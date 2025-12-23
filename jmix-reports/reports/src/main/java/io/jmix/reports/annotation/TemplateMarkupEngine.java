package io.jmix.reports.annotation;

/**
 * Type of the engine to be used to process the contents of the text template.
 *
 * @see TemplateDef
 */
public enum TemplateMarkupEngine {
    /**
     * Groovy templates.
     */
    GROOVY,

    /**
     * FreeMarker.
     */
    FREEMARKER
}
