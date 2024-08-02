package io.jmix.fullcalendarflowui.component.data;

import com.google.common.base.Strings;
import com.vaadin.flow.data.provider.Query;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.fullcalendarflowui.component.data.LazyCalendarEventProvider.ItemsFetchContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Component("fclndr_LazyCalendarItems")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityCalendarEventRetriever extends AbstractEntityEventProvider<ItemsFetchContext>
        implements LazyCalendarEventProvider, ApplicationContextAware {

    protected static final String START_DATE_PARAMETER = "fetchStartDate";
    protected static final String END_DATE_PARAMETER = "fetchEndDate";

    protected DataManager dataManager;
    protected Metadata metadata;
    protected DateTimeTransformations dateTimeTransformations;

    protected Class<?> entityClass;
    protected String queryString;
    protected FetchPlan fetchPlan;

    protected Function<ItemsFetchContext, List<CalendarEvent>> loadDelegate;

    public EntityCalendarEventRetriever() {
    }

    public EntityCalendarEventRetriever(String id) {
        super(id);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        dataManager = applicationContext.getBean(DataManager.class);
        metadata = applicationContext.getBean(Metadata.class);
        dateTimeTransformations = applicationContext.getBean(DateTimeTransformations.class);
    }

    @Nullable
    public Function<ItemsFetchContext, List<CalendarEvent>> getLoadDelegate() {
        return loadDelegate;
    }

    public void setLoadDelegate(@Nullable Function<ItemsFetchContext, List<CalendarEvent>> loadDelegate) {
        this.loadDelegate = loadDelegate;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        if (getEntityClass() == null) {
            throw new IllegalStateException("Entity class is not set");
        }
        return metadata.getClass(getEntityClass());
    }

    /**
     * @return entity class or {@code null} if not set
     */
    @Nullable
    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        Preconditions.checkNotNullArgument(entityClass);
        this.entityClass = entityClass;
    }

    /**
     * @return query string or {@code null} if not set
     */
    @Nullable
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        Preconditions.checkNotEmptyString(queryString);
        this.queryString = queryString;
    }

    @Nullable
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(@Nullable FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<CalendarEvent, ItemsFetchContext> query) {
        ItemsFetchContext fetchContext = query.getFilter()
                .orElseThrow(() -> new IllegalArgumentException("Filter required"));

        return load(fetchContext).size();
    }

    @Override
    public Stream<CalendarEvent> fetch(Query<CalendarEvent, ItemsFetchContext> query) {
        ItemsFetchContext fetchContext = query.getFilter()
                .orElseThrow(() -> new IllegalArgumentException("Filter required"));

        return load(fetchContext).stream();
    }

    @Override
    public List<CalendarEvent> onItemsFetch(ItemsFetchContext context) {
        return fetch(new Query<>(context)).toList();
    }

    @Override
    public Class<?> getStartPropertyJavaType() {
        if (Strings.isNullOrEmpty(getStartDateTimeProperty())) {
            return null;
        }
        MetaProperty property = getEntityMetaClass().getProperty(getStartDateTimeProperty());
        return property.getJavaType();
    }

    @Override
    public Class<?> getEndPropertyJavaType() {
        if (Strings.isNullOrEmpty(getEndDateTimeProperty())) {
            return null;
        }
        MetaProperty property = getEntityMetaClass().getProperty(getEndDateTimeProperty());
        return property.getJavaType();
    }

    protected List<CalendarEvent> load(ItemsFetchContext fetchContext) {
        if (loadDelegate != null) {
            return loadDelegate.apply(fetchContext);
        }

        return loadItems(fetchContext).stream()
                .map(item -> (CalendarEvent) new EntityCalendarEvent<>(item, this))
                .toList();
    }

    protected List<?> loadItems(ItemsFetchContext fetchContext) {
        if (dataManager == null) {
            // todo log warn
            return Collections.emptyList();
        }
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class is required or set load delegate function instead");
        }

        FluentLoader.ByQuery<?> builder = dataManager
                .load(entityClass)
                .query(queryString)
                .fetchPlan(fetchPlan);

        if (isStartDateUsed(queryString)) {
            builder.parameter(START_DATE_PARAMETER, getStartParameterValue(fetchContext));
        }
        if (isEndDateUsed(queryString)) {
            builder.parameter(END_DATE_PARAMETER, getEndParameterValue(fetchContext));
        }
        return builder.list();
    }

    protected Object getStartParameterValue(ItemsFetchContext fetchContext) {
        String startProperty = getStartDateTimeProperty();
        if (Strings.isNullOrEmpty(startProperty)) {
            throw new IllegalStateException("Cannot convert start bound to type of startProperty");
        }
        return transformToValueWithSystemTimeZone(fetchContext.getStartDateTime(), startProperty);
    }

    protected Object getEndParameterValue(ItemsFetchContext fetchContext) {
        String endProperty = getEndDateTimeProperty();
        if (Strings.isNullOrEmpty(endProperty)) {
            throw new IllegalStateException("Cannot convert end bound to type of endProperty");
        }
        return transformToValueWithSystemTimeZone(fetchContext.getEndDateTime(), endProperty);
    }

    protected Object transformToValueWithSystemTimeZone(LocalDateTime value, String propertyName) {
        MetaProperty metaProperty = metadata.getClass(entityClass).getProperty(propertyName);
        Class<?> propertyType = metaProperty.getJavaType();

        return dateTimeTransformations.transformToType(value, propertyType, null);
    }

    protected boolean isStartDateUsed(String query) {
        return query.contains(START_DATE_PARAMETER);
    }

    protected boolean isEndDateUsed(String query) {
        return query.contains(END_DATE_PARAMETER);
    }
}
