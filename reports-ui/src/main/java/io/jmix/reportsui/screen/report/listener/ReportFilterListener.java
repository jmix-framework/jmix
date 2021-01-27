package io.jmix.reportsui.screen.report.listener;

import io.jmix.reports.ParameterClassResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("report_ReportFilterListener")
public class ReportFilterListener {

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

//
//    public void windowClosed(String actionId) {
//        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
//            filterEntity = filterEditor.getFilterEntity();
//            collectQueryAndParametersFromFilter();
//        }
//    }
//
//    protected void collectQueryAndParametersFromFilter() {
//        FilterParser filterParser = AppBeans.get(FilterParser.class);
//        filterEntity.setXml(filterParser.getXml(filterEditor.getConditions(), Param.ValueProperty.DEFAULT_VALUE));
//        if (filterEntity.getXml() != null) {
//            Element element = Dom4j.readDocument(filterEntity.getXml()).getRootElement();
    //TODO new query filter
//                        QueryFilter queryFilter = new QueryFilter(element);
//                        conditionsTree = filterEditor.getConditionsTree();
//                        filter = filterEditor.getFilter();
//                        wizard.query = collectQuery(queryFilter);
//                        wizard.queryParameters = collectQueryParameters(queryFilter);
//        } else {
//            wizard.notifications.create(Notifications.NotificationType.HUMANIZED)
//                    .withCaption(wizard.getMessage("defaultQueryHasBeenSet"))
//                    .show();
//            wizard.query = filter.getDatasource().getQuery();
//            wizard.queryParameters = Collections.emptyList();
//        }
//
//        wizard.setQueryButton.setCaption(wizard.getMessage("changeQuery"));
//    }
//
//    protected List<ReportData.Parameter> collectQueryParameters(QueryFilter queryFilter) {
//        List<ReportData.Parameter> newParametersList = new ArrayList<>();
//        int i = 1;
//        for (ParameterInfo parameterInfo : queryFilter.getCompiledParameters()) {
//            Condition condition = findConditionByParameter(queryFilter.getRoot(), parameterInfo);
//            String conditionName = parameterInfo.getConditionName();
//            if (conditionName == null) {
//                conditionName = "parameter";
//            }
//
//            Boolean hiddenConditionPropertyValue = findHiddenPropertyValueByConditionName(conditionName);
//            TemporalType temporalType = getTemporalType(conditionName);
//
//            conditionName = conditionName.replaceAll("\\.", "_");
//
//            String parameterName = conditionName + i;
//            i++;
//            Class parameterClass = parameterInfo.getJavaClass();
//            ParameterType parameterType = getParameterType(parameterInfo, temporalType, parameterClass);
//
//            String parameterValue = parameterInfo.getValue();
//            parameterValue = !"NULL".equals(parameterValue) ? parameterValue : null;
//
//            newParametersList.add(new ReportData.Parameter(
//                    parameterName,
//                    parameterClass,
//                    parameterType,
//                    parameterValue,
//                    resolveParameterTransformation(condition),
//                    hiddenConditionPropertyValue));
//
//            wizard.query = wizard.query.replace(":" + parameterInfo.getName(), "${" + parameterName + "}");
//        }
//        return newParametersList;
//    }
//
//    protected ParameterType getParameterType(ParameterInfo parameterInfo, TemporalType temporalType, Class parameterClass) {
//        ParameterType parameterType;
//
//        if (temporalType != null) {
//            switch (temporalType) {
//                case TIME:
//                    parameterType = ParameterType.TIME;
//                    break;
//                case DATE:
//                    parameterType = ParameterType.DATE;
//                    break;
//                case TIMESTAMP:
//                    parameterType = ParameterType.DATETIME;
//                    break;
//                default:
//                    parameterType = parameterClassResolver.resolveParameterType(parameterClass);
//            }
//        } else {
//            parameterType = parameterClassResolver.resolveParameterType(parameterClass);
//        }
//
//        if (parameterType == null) {
//            parameterType = ParameterType.TEXT;
//        }

//        if (parameterType == ParameterType.ENTITY) {
//            boolean inExpr = conditionsTree.toConditionsList().stream()
//                    .filter(cond -> Objects.nonNull(cond.getParamName()))
//                    .filter(cond -> cond.getParamName().equals(parameterInfo.getName()))
//                    .map(AbstractCondition::getInExpr)
//                    .findFirst()
//                    .orElse(Boolean.FALSE);
//            if (inExpr) {
//                parameterType = ParameterType.ENTITY_LIST;
//            }
//        }
//        return parameterType;
//    }

//    protected String collectQuery(QueryFilter queryFilter) {
//        Collection<ParameterInfo> parameterDescriptorsFromFilter = queryFilter.getCompiledParameters();
//        Map<String, Object> params = new HashMap<>();
//        for (ParameterInfo parameter : parameterDescriptorsFromFilter) {
//            params.put(parameter.getName(), "___");
//        }
//        return queryFilter.processQuery(filter.getDatasource().getQuery(), params);
//    }
//
//    protected Condition findConditionByParameter(Condition condition, ParameterInfo parameterInfo) {
//        if (!(condition instanceof LogicalCondition)) {
    //TODO compiled parameters
//                        Set<ParameterInfo> parameters = condition.getCompiledParameters();
//                        if (parameters != null && parameters.contains(parameterInfo)) {
//                            return condition;
//                        }
//        }
    //TODO find conditions
//                    if (condition.getConditions() != null) {
//                        for (Condition it : condition.getConditions()) {
//                            return findConditionByParameter(it, parameterInfo);
//                        }
//                    }
//        return null;
//    }
//
//    protected PredefinedTransformation resolveParameterTransformation(Condition condition) {
//        if (condition instanceof Clause) {
//            Clause clause = (Clause) condition;
//            if (clause.getOperator() != null) {
//                switch (clause.getOperator()) {
//                    case STARTS_WITH:
//                        return PredefinedTransformation.STARTS_WITH;
//                    case ENDS_WITH:
//                        return PredefinedTransformation.ENDS_WITH;
//                    case CONTAINS:
//                        return PredefinedTransformation.CONTAINS;
//                    case DOES_NOT_CONTAIN:
//                        return PredefinedTransformation.CONTAINS;
//                }
//            }
//        }
//        return null;
//    }

//    protected Boolean findHiddenPropertyValueByConditionName(String propertyName) {
//        return conditionsTree.toConditionsList().stream()
//                .filter(condition -> Objects.nonNull(condition.getName()))
//                .filter(condition -> condition.getName().equals(propertyName))
//                .map(AbstractCondition::getHidden)
//                .findFirst()
//                .orElse(Boolean.FALSE);
//    }

//    protected TemporalType getTemporalType(String propertyName) {
//        for (AbstractCondition condition : conditionsTree.toConditionsList()) {
//            if (condition.getName() != null && condition.getName().equals(propertyName)
//                    && condition.getParam() != null && condition.getParam().getProperty() != null) {
//                Map annotations = condition.getParam().getProperty().getAnnotations();
//                return (TemporalType) annotations.get(MetadataTools.TEMPORAL_ANN_NAME);
//            }
//        }
//        return null;
//    }
}

