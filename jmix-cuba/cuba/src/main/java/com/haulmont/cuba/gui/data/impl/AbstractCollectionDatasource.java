/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.global.filter.ParameterInfo;
import com.haulmont.cuba.core.global.filter.ParametersHelper;
import com.haulmont.cuba.core.global.filter.QueryFilter;
import com.haulmont.cuba.core.global.impl.UserIdUtils;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.security.global.UserSession;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.FrameContext;
import io.jmix.ui.component.HasValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @param <T> type of entity
 * @param <K> type of entity ID
 */
@Deprecated
public abstract class AbstractCollectionDatasource<T extends Entity, K>
        extends DatasourceImpl<T>
        implements CollectionDatasource<T, K>,
        CollectionDatasource.SupportsRefreshMode<T, K> {

    protected String query;
    protected QueryFilter filter;
    protected int maxResults;
    protected ParameterInfo[] queryParameters;
    protected boolean softDeletion = true;
    protected boolean cacheable;
    protected ComponentValueListener componentValueListener;
    protected boolean refreshOnComponentValueChange;
    protected Sortable.SortInfo<MetaPropertyPath>[] sortInfos;
    protected Map<String, Object> savedParameters;
    protected Throwable dataLoadError;
    protected boolean listenersSuspended;
    protected final LinkedList<CollectionChangeEvent<T, K>> suspendedEvents = new LinkedList<>();
    protected RefreshMode refreshMode = RefreshMode.ALWAYS;
    protected UserSession userSession = AppBeans.get(UserSessionSource.class).getUserSession();

    @Override
    public T getItemNN(K id) {
        T it = getItem(id);
        if (it != null)
            return it;
        else
            throw new NullPointerException("Item with id=" + id + " is not found in datasource " + this.id);
    }

    @Override
    public void setItem(T item) {
        backgroundWorker.checkUIAccess();

        if (State.VALID.equals(state)) {
            T prevItem = this.item;

            if (prevItem != item) {
                if (item != null) {
                    final MetaClass aClass = metadata.getClass(item);
                    if (!aClass.equals(this.metaClass) && !this.metaClass.getDescendants().contains(aClass)) {
                        throw new DevelopmentException(String.format("Invalid item metaClass '%s'", aClass));
                    }
                }
                this.item = item;

                fireItemChanged(prevItem);
            }
        }
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public LoadContext getCompiledLoadContext() throws UnsupportedOperationException {
        return null;
    }

    @Override
    public QueryFilter getQueryFilter() {
        return filter;
    }

    @Override
    public void setQuery(String query) {
        setQuery(query, null);
    }

    @Override
    public void setQueryFilter(QueryFilter filter) {
        String query = getQuery();
        if (query == null) {
            throw new DevelopmentException("Unable to use filter on '" + getId() + "' datasource without query");
        }
        setQuery(query, filter);
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public Map<String, Object> getLastRefreshParameters() {
        return savedParameters == null ?
                Collections.emptyMap() :
                Collections.unmodifiableMap(savedParameters);
    }

    @Override
    public boolean getRefreshOnComponentValueChange() {
        return refreshOnComponentValueChange;
    }

    @Override
    public void setRefreshOnComponentValueChange(boolean refresh) {
        refreshOnComponentValueChange = refresh;
    }

    @Override
    public void setQuery(String query, QueryFilter filter) {
        if (Objects.equals(this.query, query) && Objects.equals(this.filter, filter))
            return;

        this.query = query;
        this.filter = filter;

        queryParameters = ParametersHelper.parseQuery(query, filter);

        for (ParameterInfo info : queryParameters) {
            final ParameterInfo.Type type = info.getType();
            if (ParameterInfo.Type.DATASOURCE.equals(type)) {
                final String path = info.getPath();

                final String[] strings = path.split("\\.");
                String source = strings[0];

                final String property;
                if (strings.length > 1) {
                    final List<String> list = Arrays.asList(strings);
                    final List<String> valuePath = list.subList(1, list.size());
                    String[] path1 = valuePath.toArray(new String[0]);
                    property = ObjectPathUtils.formatValuePath(path1);
                } else {
                    property = null;
                }

                final Datasource ds = dsContext.get(source);
                if (ds != null) {
                    dsContext.registerDependency(this, ds, property);
                } else {
                    ((DsContextImplementation) dsContext).addLazyTask(context -> {
                        final String[] strings1 = path.split("\\.");
                        String source1 = strings1[0];

                        final Datasource ds1 = dsContext.get(source1);
                        if (ds1 != null) {
                            dsContext.registerDependency(AbstractCollectionDatasource.this, ds1, property);
                        }
                    });
                }
            }
        }
    }

    protected Map<String, Object> getQueryParameters(Map<String, Object> params) {
        final Map<String, Object> map = new HashMap<>();
        for (ParameterInfo info : queryParameters) {
            String name = info.getFlatName();

            final String path = info.getPath();
            final String[] elements = path.split("\\.");
            switch (info.getType()) {
                case DATASOURCE: {
                    String dsName = elements[0];
                    final Datasource datasource = dsContext.get(dsName);
                    if (datasource == null) {
                        throw new DevelopmentException("Datasource '" + dsName + "' not found in dsContext",
                                "datasource", dsName);
                    }

                    if (datasource.getState() == State.VALID) {
                        final Entity item = datasource.getItem();
                        if (elements.length > 1) {
                            String[] valuePath = ArrayUtils.subarray(elements, 1, elements.length);
                            String propertyName = ObjectPathUtils.formatValuePath(valuePath);
                            Object value = EntityValues.getValueEx(item, propertyName);
                            map.put(name, value);
                        } else {
                            map.put(name, item);
                        }
                    } else {
                        map.put(name, null);
                    }

                    break;
                }
                case PARAM: {
                    Object value;
                    if (dsContext.getFrameContext() == null) {
                        value = null;
                    } else {
                        Map<String, Object> windowParams = dsContext.getFrameContext().getParams();
                        value = windowParams.get(path);
                        if (value == null && elements.length > 1) {
                            Entity entity = (Entity) windowParams.get(elements[0]);
                            if (entity != null) {
                                String[] valuePath = ArrayUtils.subarray(elements, 1, elements.length);
                                String propertyName = ObjectPathUtils.formatValuePath(valuePath);
                                value = EntityValues.getValueEx(entity, propertyName);
                            }
                        }
                    }
                    if (value instanceof String && info.isCaseInsensitive()) {
                        value = makeCaseInsensitive((String) value);
                    }
                    map.put(name, value);
                    break;
                }
                case COMPONENT: {
                    Object value = null;
                    if (dsContext.getFrameContext() != null) {
                        value = dsContext.getFrameContext().getValue(path);
                        if (value instanceof String && info.isCaseInsensitive()) {
                            value = makeCaseInsensitive((String) value);
                        }
                        if (java.sql.Date.class.equals(info.getJavaClass()) && value instanceof Date) {
                            value = new java.sql.Date(((Date) value).getTime());
                        }
                        if (refreshOnComponentValueChange) {
                            if (componentValueListener == null)
                                componentValueListener = new ComponentValueListener();
                            try {
                                dsContext.getFrameContext().addValueChangeListener(path, componentValueListener);
                            } catch (Exception e) {
                                Logger log = LoggerFactory.getLogger(AbstractCollectionDatasource.class);
                                log.error("Unable to add value listener: " + e);
                            }
                        }
                    }
                    map.put(name, value);
                    break;
                }
                case SESSION: {
                    Object value;
                    value = userSession.getAttribute(path);
                    if (value instanceof String && info.isCaseInsensitive()) {
                        value = makeCaseInsensitive((String) value);
                    }
                    map.put(name, value);
                    break;
                }
                case CUSTOM: {
                    Object value = params.get(info.getPath());
                    if (value == null) {
                        //a case when a query contains a parameter like :custom$city.country.id and we passed
                        //just "city" parameter to the datasource refresh() method
                        String[] pathElements = info.getPath().split("\\.");
                        if (pathElements.length > 1) {
                            Object entity = params.get(pathElements[0]);
                            if (entity instanceof Entity) {
                                value = EntityValues.getValueEx((Entity) entity, Arrays.copyOfRange(pathElements, 1, pathElements.length));
                            }
                        }
                    }
                    if (value instanceof String && info.isCaseInsensitive()) {
                        value = makeCaseInsensitive((String) value);
                    }
                    map.put(name, value);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("Unsupported parameter type: " + info.getType());
                }
            }
        }

        return map;
    }

    protected String makeCaseInsensitive(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(ParametersHelper.CASE_INSENSITIVE_MARKER);
        if (!value.startsWith("%"))
            sb.append("%");
        sb.append(value);
        if (!value.endsWith("%"))
            sb.append("%");
        return sb.toString();
    }

    protected String getJPQLQuery(Map<String, Object> parameterValues) {
        String query;
        if (filter == null)
            query = this.query;
        else
            query = filter.processQuery(this.query, parameterValues);

        for (ParameterInfo info : queryParameters) {
            String paramName = info.getName();
            String jpaParamName = info.getFlatName();

            Pattern p = Pattern.compile(paramName.replace("$", "\\$") + "([^.]|$)"); // not ending with "."
            Matcher m = p.matcher(query);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, jpaParamName + "$1");
            }
            m.appendTail(sb);
            query = sb.toString();

            Object value = parameterValues.get(paramName);
            if (value != null) {
                parameterValues.put(jpaParamName, value);
            }
        }
        query = query.replace(":" + ParametersHelper.CASE_INSENSITIVE_MARKER, ":");

        // todo filter freemarker
//        query = TemplateHelper.processTemplate(query, parameterValues);

        return query;
    }

    protected void fireCollectionChanged(CollectionDatasource.Operation operation, List<T> items) {
        if (listenersSuspended) {
            if (!suspendedEvents.isEmpty() && suspendedEvents.getFirst().getOperation().equals(operation)) {
                suspendedEvents.getFirst().getItems().addAll(items);
            } else {
                suspendedEvents.addFirst(new CollectionChangeEvent<>(this, operation, new ArrayList<>(items)));
            }
            return;
        }

        CollectionChangeEvent<T, K> collectionChangeEvent = new CollectionChangeEvent<>(this, operation, items);
        //noinspection unchecked
        getEventRouter().fireEvent(CollectionChangeListener.class, CollectionChangeListener::collectionChanged, collectionChangeEvent);
    }

    protected Map<String, Object> getTemplateParams(Map<String, Object> customParams) {

        Map<String, Object> templateParams = new HashMap<>();

        String compPrefix = ParameterInfo.Type.COMPONENT.getPrefix() + "$";
        for (ParameterInfo info : queryParameters) {
            if (info.getType() == ParameterInfo.Type.COMPONENT) {
                Object value = dsContext.getFrameContext() == null ?
                        null : dsContext.getFrameContext().getValue(info.getPath());
                templateParams.put(compPrefix + info.getPath(), value);
            }
        }

        String customPrefix = ParameterInfo.Type.CUSTOM.getPrefix() + "$";
        for (Map.Entry<String, Object> entry : customParams.entrySet()) {
            templateParams.put(customPrefix + entry.getKey(), entry.getValue());
        }

        if (dsContext != null) {
            FrameContext windowContext = dsContext.getFrameContext();
            if (windowContext != null) {
                String paramPrefix = ParameterInfo.Type.PARAM.getPrefix() + "$";
                for (Map.Entry<String, Object> entry : windowContext.getParams().entrySet()) {
                    templateParams.put(paramPrefix + entry.getKey(), entry.getValue());
                }
            }
        }

        String sessionPrefix = ParameterInfo.Type.SESSION.getPrefix() + "$";
        if (UserIdUtils.hasUserId(userSession.getUser())) {
            templateParams.put(sessionPrefix + "userId", UserIdUtils.getUserId(userSession.getUser()));
        }
        templateParams.put(sessionPrefix + "userLogin", userSession.getUser().getUsername());
        for (String name : userSession.getAttributeNames()) {
            templateParams.put(sessionPrefix + name, userSession.getAttribute(name));
        }

        return templateParams;
    }

    @Override
    public void suspendListeners() {
        listenersSuspended = true;
    }

    @Override
    public void resumeListeners() {
        listenersSuspended = false;

        while (!suspendedEvents.isEmpty()) {
            CollectionChangeEvent<T, K> suspendedEvent = suspendedEvents.removeLast();
            fireCollectionChanged(suspendedEvent.getOperation(), Collections.unmodifiableList(suspendedEvent.getItems()));
        }
    }

    @Override
    public void mute() {
        listenersSuspended = true;
    }

    @Override
    public void unmute(UnmuteEventsMode mode) {
        listenersSuspended = false;

        if (mode == UnmuteEventsMode.FIRE_REFRESH_EVENT) {
            fireCollectionChanged(Operation.REFRESH, Collections.emptyList());
        }
    }

    @Override
    public boolean isSoftDeletion() {
        return softDeletion;
    }

    @Override
    public void setSoftDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public void commit() {
        backgroundWorker.checkUIAccess();

        if (!allowCommit) {
            return;
        }

        if (getCommitMode() == CommitMode.DATASTORE) {
            DataSupplier supplier = getDataSupplier();

            CommitContext context = new CommitContext();
            for (Entity entity : itemsToCreate) {
                context.addInstanceToCommit(entity, view);
            }
            for (Entity entity : itemsToUpdate) {
                context.addInstanceToCommit(entity, view);
            }
            for (Entity entity : itemsToDelete) {
                context.addInstanceToRemove(entity);
            }

            Set<Entity> committed = supplier.commit(context);

            committed(committed);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected Comparator<T> createEntityComparator() {
        // In case of generated column the sortInfos can actually contain string as a column identifier.
        if (sortInfos[0].getPropertyPath() != null) {
            final MetaPropertyPath propertyPath = sortInfos[0].getPropertyPath();
            final boolean asc = Sortable.Order.ASC.equals(sortInfos[0].getOrder());
            return Comparator.comparing(e -> EntityValues.getValueEx(e, propertyPath), EntityValuesComparator.asc(asc));
        } else {
            // If we can not sort the datasource, just return the empty comparator.
            return (o1, o2) -> 0;
        }
    }

    protected DataLoadContextQuery createDataQuery(DataLoadContext context, Map<String, Object> params) {
        DataLoadContextQuery q = null;
        if (query != null && queryParameters != null) {
            Map<String, Object> parameters = getQueryParameters(params);
            for (ParameterInfo info : queryParameters) {
                if (ParameterInfo.Type.DATASOURCE.equals(info.getType())) {
                    Object value = parameters.get(info.getFlatName());
                    if (value == null) {
                        String[] pathElements = info.getPath().split("\\.");
                        if (pathElements.length == 1) {
                            //nothing selected in 'master' datasource, so return null here to clear the 'detail' datasource
                            return null;
                        } else {
                            //The parameter with null value is the path to the datasource item property,
                            //e.g. :ds$User.group.id.
                            //If the 'master' datasource item is not null then do not clear the 'detail' datasource,
                            //a null query parameter value should be processed
                            String dsName = pathElements[0];
                            final Datasource datasource = dsContext.get(dsName);
                            if (datasource == null) {
                                throw new DevelopmentException("Datasource '" + dsName + "' not found in dsContext",
                                        "datasource", dsName);
                            }
                            if (datasource.getState() != State.VALID || datasource.getItem() == null) return null;
                        }
                    }
                }
            }

            String queryString = getJPQLQuery(getTemplateParams(params));
            q = context.setQueryString(queryString);
            // Pass only parameters used in the resulting query
            QueryParser parser = AppBeans.get(QueryTransformerFactory.class).parser(queryString);
            Set<String> paramNames = parser.getParamNames();
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (paramNames.contains(entry.getKey()))
                    q.setParameter(entry.getKey(), entry.getValue());
            }
        } else if (!(context instanceof ValueLoadContext)) {
            Collection<MetaProperty> properties = metadata.getTools().getInstanceNameRelatedProperties(metaClass);
            if (!properties.isEmpty()) {
                String orderBy = properties.stream()
                        .filter(m -> m != null
                                && m.getAnnotatedElement().getAnnotation(JmixProperty.class) == null)
                        .map(m -> "e." + m.getName())
                        .collect(Collectors.joining(", "));

                String queryString;
                if (!orderBy.isEmpty()) {
                    queryString = String.format("select e from %s e order by %s", metaClass.getName(), orderBy);
                } else {
                    queryString = String.format("select e from %s e", metaClass.getName());
                }

                q = context.setQueryString(queryString);
            } else
                q = context.setQueryString("select e from " + metaClass.getName() + " e");
        }
        if (q instanceof LoadContext.Query) {
            ((LoadContext.Query) q).setCacheable(isCacheable());
        }
        return q;
    }

    /**
     * Return number of rows for the current query set in the datasource.
     *
     * @return number of rows. In case of error returns 0 and sets {@link #dataLoadError} field to the exception object
     */
    public int getCount() {
        LoadContext<Entity> context = new LoadContext<>(metaClass);
        LoadContext.Query q = (LoadContext.Query) createDataQuery(context, savedParameters == null ? Collections.emptyMap() : savedParameters);
        context.setSoftDeletion(isSoftDeletion());
        if (q == null)
            return 0;

        prepareLoadContext(context);

        dataLoadError = null;
        try {
            long res = dataSupplier.getCount(context);
            if (res > Integer.MAX_VALUE)
                throw new RuntimeException("Number of records is too big: " + res);
            return (int) res;
        } catch (Throwable e) {
            dataLoadError = e;
        }
        return 0;
    }

    protected String getLoggingTag(String prefix) {
        String windowId = "";
        if (dsContext != null) {
            FrameContext windowContext = dsContext.getFrameContext();
            if (windowContext != null) {
                Frame frame = windowContext.getFrame();
                if (frame != null) {
                    windowId = ComponentsHelper.getFullFrameId(windowContext.getFrame());
                }
            }
        }
        String tag = prefix + " " + id;
        if (StringUtils.isNotBlank(windowId))
            tag = windowId + "@" + id;
        return tag;
    }

    protected void prepareLoadContext(LoadContext<?> context) {
    }

    protected void checkDataLoadError() {
        if (dataLoadError != null) {
            if (dataLoadError instanceof RuntimeException)
                throw (RuntimeException) dataLoadError;
            else
                throw new RuntimeException("Data load error", dataLoadError);
        }
    }

    protected void setSortDirection(LoadContext.Query q) {
        MetaPropertyPath propertyPath = sortInfos[0].getPropertyPath();
        Sort.Direction direction = Sortable.Order.ASC.equals(sortInfos[0].getOrder()) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        q.setSort(Sort.by(direction, propertyPath.toString()));
    }

    @Override
    public RefreshMode getRefreshMode() {
        return refreshMode;
    }

    @Override
    public void setRefreshMode(RefreshMode refreshMode) {
        this.refreshMode = refreshMode;
    }

    protected class ComponentValueListener implements Consumer<HasValue.ValueChangeEvent> {
        @Override
        public void accept(HasValue.ValueChangeEvent e) {
            refresh();
        }
    }

    @Override
    public void addCollectionChangeListener(CollectionChangeListener<? super T, K> listener) {
        getEventRouter().addListener(CollectionChangeListener.class, listener);
    }

    @Override
    public void removeCollectionChangeListener(CollectionChangeListener<? super T, K> listener) {
        getEventRouter().removeListener(CollectionChangeListener.class, listener);
    }
}
