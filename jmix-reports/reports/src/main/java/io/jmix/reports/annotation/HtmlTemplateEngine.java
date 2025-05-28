package io.jmix.reports.annotation;

/**
 * Type of the engine to be used to process the HTML template.
 *
 * @see TemplateDef
 */
public enum HtmlTemplateEngine {
    /**
     * Groovy templates.
     */
    GROOVY,

    /**
     * FreeMarker.
     */
    FREEMARKER
}
