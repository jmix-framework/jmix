package io.jmix.flowui.action;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Interface to be implemented by actions that open a view.
 */
public interface ViewOpeningAction extends Action {

    /**
     * Returns the view open mode if it was set by {@link #setOpenMode(OpenMode)}
     * or in the view XML, otherwise returns {@code null}.
     */
    @Nullable
    OpenMode getOpenMode();

    /**
     * Sets the view open mode.
     *
     * @param openMode the open mode to set
     */
    void setOpenMode(@Nullable OpenMode openMode);

    /**
     * Returns the view id if it was set by {@link #setViewId(String)}
     * or in the view XML, otherwise returns {@code null}.
     */
    @Nullable
    String getViewId();

    /**
     * Sets the view id.
     *
     * @param viewId the view id to set
     */
    void setViewId(@Nullable String viewId);

    /**
     * Returns the view class if it was set by {@link #setViewClass(Class)}
     * or in the view XML, otherwise returns {@code null}.
     */
    @Nullable
    Class<? extends View> getViewClass();

    /**
     * Sets the view class.
     *
     * @param viewClass the view class to set
     */
    void setViewClass(@Nullable Class<? extends View> viewClass);

    /**
     * @return route parameters or {@code null} if not set
     */
    @Nullable
    RouteParameters getRouteParameters();

    /**
     * Sets route parameters that should be used in the route template.
     * <p>
     * Note that route parameters are set if the detail is opened in {@link OpenMode#NAVIGATION}.
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
     * Note that query parameters are set if the detail is opened in {@link OpenMode#NAVIGATION}.
     *
     * @param queryParameters query parameters to set
     */
    void setQueryParameters(@Nullable QueryParameters queryParameters);

    /**
     * Sets the handler to be invoked when the detail view closes.
     * <p>
     * Note that handler is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
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
     * @param <S>               view type
     */
    <S extends View<?>> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<S>> afterCloseHandler);
}
