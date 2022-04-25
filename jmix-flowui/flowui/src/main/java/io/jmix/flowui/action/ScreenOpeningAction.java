package io.jmix.flowui.action;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.OpenMode;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Interface to be implemented by actions that open a screen.
 */
public interface ScreenOpeningAction extends Action {

    /**
     * Returns the screen open mode if it was set by {@link #setOpenMode(OpenMode)}
     * or in the screen XML, otherwise returns {@code null}.
     */
    @Nullable
    OpenMode getOpenMode();

    /**
     * Sets the screen open mode.
     *
     * @param openMode the open mode to set
     */
    void setOpenMode(@Nullable OpenMode openMode);

    /**
     * Returns the screen id if it was set by {@link #setScreenId(String)}
     * or in the screen XML, otherwise returns {@code null}.
     */
    @Nullable
    String getScreenId();

    /**
     * Sets the screen id.
     *
     * @param screenId the screen id to set
     */
    void setScreenId(@Nullable String screenId);

    /**
     * Returns the screen class if it was set by {@link #setScreenClass(Class)}
     * or in the screen XML, otherwise returns {@code null}.
     */
    @Nullable
    Class<? extends Screen> getScreenClass();

    /**
     * Sets the screen class.
     *
     * @param screenClass the screen class to set
     */
    void setScreenClass(@Nullable Class<? extends Screen> screenClass);

    /**
     * @return route parameters or {@code null} if not set
     */
    @Nullable
    RouteParameters getRouteParameters();

    /**
     * Sets route parameters that should be used in the route template.
     * <p>
     * Note that route parameters are set if the editor is opened in {@link OpenMode#NAVIGATION}.
     *
     * @param routeParameters route parameters to set
     * @see Route
     */
    void setRouteParameters(@Nullable RouteParameters routeParameters);

    /**
     * @return query parameters or {@code null} if not set
     */
    @Nullable
    QueryParameters getQueryParameters();

    /**
     * Sets query parameters that should be used in the URL.
     * <p>
     * Note that query parameters are set if the editor is opened in {@link OpenMode#NAVIGATION}.
     *
     * @param queryParameters query parameters to set
     */
    void setQueryParameters(@Nullable QueryParameters queryParameters);

    /**
     * Sets the handler to be invoked when the editor screen closes.
     * <p>
     * Note that handler is invoked if the editor is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the handler is using a controller method
     * annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "afterCloseHandler")
     * protected void petsTableViewAfterCloseHandler(AfterCloseEvent event) {
     *     if (event.closedWith(StandardOutcome.COMMIT)) {
     *         System.out.println("Committed");
     *     }
     * }
     * </pre>
     *
     * @param afterCloseHandler handler to set
     * @param <S>               screen type
     */
    <S extends Screen> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<S>> afterCloseHandler);
}
