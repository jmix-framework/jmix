package io.jmix.flowui.view.navigation;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility bean that facilitates url modifications and generations.
 */
@Component("flowui_RouteSupport")
public class RouteSupport {

    private static final Logger log = LoggerFactory.getLogger(RouteSupport.class);

    protected UrlParamSerializer urlParamSerializer;
    protected ServletContext servletContext;

    public RouteSupport(UrlParamSerializer urlParamSerializer, ServletContext servletContext) {
        this.urlParamSerializer = urlParamSerializer;
        this.servletContext = servletContext;
    }

    /**
     * Retrieves the current url from the browser and converts it to the {@link Location}
     * object. The URL is fetched from the browser in another request asynchronously and
     * the {@link Location} object passed to the callback.
     *
     * @param ui       UI instance for which to fetch current location
     * @param callback to be notified when the location is resolved
     */
    public void fetchCurrentLocation(UI ui, Consumer<Location> callback) {
        ui.getPage().fetchCurrentURL(url -> {
            log.debug("Fetched URL: {}", url.toString());

            String locationString = resolveLocationString(url);
            Location location = new Location(locationString, resolveQueryParameters(url));
            callback.accept(location);
        });
    }

    /**
     * Updates the current url by adding the value of the given query parameter.
     * If a query parameter with the same name already exists, the new value is
     * added excluding duplicates, otherwise the query parameter is added.
     *
     * @param ui    UI instance for which to update url
     * @param name  the query parameter name
     * @param value the query parameter value
     */
    public void addQueryParameter(UI ui, String name, Object value) {
        addQueryParameter(ui, name, List.of(value));
    }

    /**
     * Updates the current url by adding the value of the given query parameter.
     * If a query parameter with the same name already exists, the new value is
     * added excluding duplicates, otherwise the query parameter is added.
     *
     * @param ui     UI instance for which to update url
     * @param name   the query parameter name
     * @param values the query parameter values
     */
    public void addQueryParameter(UI ui, String name, List<Object> values) {
        updateQueryParameters(ui, name, values, this::addQueryParameter);
    }

    /**
     * Updates the current url by setting the value of the given query parameter.
     * If a query parameter with the same name already exists, its value is
     * overridden, otherwise the query parameter is added.
     *
     * @param ui    UI instance for which to update url
     * @param name  the query parameter name
     * @param value the query parameter value
     */
    public void setQueryParameter(UI ui, String name, Object value) {
        setQueryParameter(ui, name, List.of(value));
    }

    /**
     * Updates the current url by setting the value of the given query parameter.
     * If a query parameter with the same name already exists, its value is
     * overridden, otherwise the query parameter is added.
     *
     * @param ui     UI instance for which to update url
     * @param name   the query parameter name
     * @param values the query parameter values
     */
    public void setQueryParameter(UI ui, String name, List<Object> values) {
        updateQueryParameters(ui, name, values, this::setQueryParameter);
    }

    protected void updateQueryParameters(UI ui, String name, List<Object> values,
                                         TriFunction<QueryParameters, String, List<Object>, QueryParameters> updater) {
        Page page = ui.getPage();
        page.fetchCurrentURL(url -> {
            log.debug("Fetched URL: {}", url.toString());

            String locationString = resolveLocationString(url);
            QueryParameters resultQueryParameters =
                    updater.apply(resolveQueryParameters(url), name, values);
            Location newLocation = new Location(locationString, resultQueryParameters);

            log.debug("Replace URL state with new location: {}", newLocation.getPathWithQueryParameters());
            page.getHistory().replaceState(null, newLocation);
        });
    }

    /**
     * Overrides the current url query parameters with the values represented by {@link QueryParameters} object.
     *
     * @param ui              UI instance for which to update url
     * @param queryParameters an object which holds query parameters information
     */
    public void setQueryParameters(UI ui, QueryParameters queryParameters) {
        Page page = ui.getPage();
        page.fetchCurrentURL(url -> {
            log.debug("Fetched URL: {}", url.toString());

            String locationString = resolveLocationString(url);
            Location newLocation = new Location(locationString, queryParameters);

            log.debug("Replace URL state with new location: {}", newLocation.getPathWithQueryParameters());
            page.getHistory().replaceState(null, newLocation);
        });
    }

    /**
     * Creates {@link RouteParameters} object which stores a single route parameter with given name and value.
     *
     * @param param the route parameter name
     * @param value the route parameter name
     * @return an object which stores the route parameters
     */
    public RouteParameters createRouteParameters(String param, Object value) {
        return new RouteParameters(ImmutableMap.of(param, urlParamSerializer.serialize(value)));
    }

    /**
     * Creates a new {@link QueryParameters} object by merging passed query parameters into one.
     * If several objects have the same query parameter information, the latest have precedence.
     *
     * @param parameters query parameters to merge
     * @return a new {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters mergeQueryParameters(QueryParameters... parameters) {
        Map<String, List<String>> combinedParams = new HashMap<>();

        for (QueryParameters queryParameters : parameters) {
            combinedParams.putAll(queryParameters.getParameters());
        }

        return new QueryParameters(combinedParams);
    }

    /**
     * Creates a new {@link QueryParameters} object by adding to it the value of the
     * given query parameter. If a query parameter with the same name already exists,
     * the new value is added excluding duplicates, otherwise the query parameter is
     * added.
     *
     * @param queryParameters the {@link QueryParameters} object to update
     * @param name            the query parameter name
     * @param value           the query parameter value
     * @return a new {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters addQueryParameter(QueryParameters queryParameters, String name, Object value) {
        return addQueryParameter(queryParameters, name, List.of(value));
    }

    /**
     * Creates a new {@link QueryParameters} object by adding to it the value of the
     * given query parameter. If a query parameter with the same name already exists,
     * the new value is added excluding duplicates, otherwise the query parameter is
     * added.
     *
     * @param queryParameters the {@link QueryParameters} object to update
     * @param name            the query parameter name
     * @param values          the query parameter values
     * @return a new {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters addQueryParameter(QueryParameters queryParameters, String name, List<Object> values) {
        Map<String, List<String>> resultParams = new HashMap<>(queryParameters.getParameters());
        List<String> convertedValues = convertValues(values);

        if (resultParams.containsKey(name)) {
            List<String> resultValues = new ArrayList<>(resultParams.get(name));
            resultValues.addAll(convertedValues);

            resultParams.put(name, resultValues.stream().distinct().collect(Collectors.toList()));
        } else {
            resultParams.put(name, convertedValues);
        }

        return new QueryParameters(resultParams);
    }

    /**
     * Creates a new {@link QueryParameters} object by setting to it the value of the
     * given query parameter. If a query parameter with the same name already exists,
     * its value is overridden, otherwise the query parameter is added.
     *
     * @param queryParameters the {@link QueryParameters} object to update
     * @param name            the query parameter name
     * @param value           the query parameter value
     * @return a new {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters setQueryParameter(QueryParameters queryParameters, String name, Object value) {
        return setQueryParameter(queryParameters, name, List.of(value));
    }

    /**
     * Creates a new {@link QueryParameters} object by setting to it the value of the
     * given query parameter. If a query parameter with the same name already exists,
     * its value is overridden, otherwise the query parameter is added.
     *
     * @param queryParameters the {@link QueryParameters} object to update
     * @param name            the query parameter name
     * @param values          the query parameter values
     * @return a new {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters setQueryParameter(QueryParameters queryParameters, String name, List<Object> values) {
        Map<String, List<String>> resultParams = new HashMap<>(queryParameters.getParameters());
        resultParams.put(name, convertValues(values));

        return new QueryParameters(resultParams);
    }

    /**
     * Creates a string containing a relative URL excluding query parameters.
     *
     * @param url a {@link URL} object to obtain location
     * @return a string representing a relative URL excluding query parameters.
     */
    public String resolveLocationString(URL url) {
        String path = url.getPath();
        String contextPath = servletContext.getContextPath();
        if (!Strings.isNullOrEmpty(contextPath)
                && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        } else {
            return path;
        }
    }

    /**
     * Creates {@link QueryParameters} object from the query part of the given {@link URL} object.
     *
     * @param url a {@link URL} object to obtain query parameters
     * @return a {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters resolveQueryParameters(URL url) {
        return resolveQueryParameters(url.getQuery());
    }

    /**
     * Creates {@link QueryParameters} object from a query string.
     *
     * @param queryString a string to parse
     * @return a {@link QueryParameters} object which holds query parameters information.
     */
    public QueryParameters resolveQueryParameters(String queryString) {
        // Empty string results in query parameter with name "" rather QueryParameters.empty()
        return QueryParameters.fromString(Strings.emptyToNull(queryString));
    }

    protected List<String> convertValues(List<Object> values) {
        return values.stream()
                .distinct()
                .map(value -> urlParamSerializer.serialize(value))
                .collect(Collectors.toList());
    }
}
