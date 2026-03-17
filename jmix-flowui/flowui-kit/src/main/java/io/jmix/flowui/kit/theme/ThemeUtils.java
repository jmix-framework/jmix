package io.jmix.flowui.kit.theme;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.theme.lumo.Lumo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class related to theme handling. It requires the
 * '@JsModule("./src/theme/color-scheme-switching-support.js")'
 * import to be added to the main application class.
 */
public final class ThemeUtils {

    private static final Logger log = LoggerFactory.getLogger(ThemeUtils.class);

    /**
     * The web storage key for which the theme value will be set
     */
    public static final String THEME_STORAGE_KEY = "jmix.app.theme";

    public static final String APPLY_THEME_FUNCTION = "window.applyTheme()";

    private static final String STORAGE = "localStorage";

    private ThemeUtils() {
    }

    /**
     * Applies the 'System' theme variant for the current {@link UI} which means
     * that the theme variant will switch between light and dark themes depending
     * on OS setting (via the browsers 'prefers-color-scheme').
     */
    public static void applySystemTheme() {
        applySystemTheme(UI.getCurrent());
    }

    /**
     * Applies the 'System' theme variant for the given {@link UI} which means
     * that the theme variant will switch between light and dark themes depending
     * on OS setting (via the browsers 'prefers-color-scheme').
     *
     * @param ui the UI for which apply the 'System' theme variant
     */
    public static void applySystemTheme(UI ui) {
        applyTheme(ui, null);
    }

    /**
     * Applies the 'Light' theme variant for the current {@link UI}.
     */
    public static void applyLightTheme() {
        applyLightTheme(UI.getCurrent());
    }

    /**
     * Applies the 'Light' theme variant for the given {@link UI}.
     *
     * @param ui the UI for which apply the 'Light' theme variant
     */
    public static void applyLightTheme(UI ui) {
        applyTheme(ui, Lumo.LIGHT);
    }

    /**
     * Applies the 'Dark' theme variant for the current {@link UI}.
     */
    public static void applyDarkTheme() {
        applyDarkTheme(UI.getCurrent());
    }

    /**
     * Applies the 'Dark' theme variant for the given {@link UI}.
     *
     * @param ui the UI for which apply the 'Dark' theme variant
     */
    public static void applyDarkTheme(UI ui) {
        applyTheme(ui, Lumo.DARK);
    }

    /**
     * Applies the given theme variant for the current {@link UI}.
     *
     * @param theme the theme variant to apply, or {@code null} to
     *              remove the theme variant value from web storage
     */
    public static void applyTheme(@Nullable String theme) {
        applyTheme(UI.getCurrent(), theme);
    }

    /**
     * Applies the given theme variant for the given {@link UI}.
     *
     * @param ui    the UI for which apply the given theme variant
     * @param theme the theme variant to apply, or {@code null} to
     *              remove the theme variant value from web storage
     */
    public static void applyTheme(UI ui, @Nullable String theme) {
        log.debug("Applying theme '{}'", theme);

        (theme != null
                ? ui.getPage().executeJs("window[$0].setItem($1,$2)", STORAGE, THEME_STORAGE_KEY, theme)
                : ui.getPage().executeJs("window[$0].removeItem($1)", STORAGE, THEME_STORAGE_KEY)
        ).then(__ -> {
            ui.getPage().executeJs(APPLY_THEME_FUNCTION);
        });
    }
}
