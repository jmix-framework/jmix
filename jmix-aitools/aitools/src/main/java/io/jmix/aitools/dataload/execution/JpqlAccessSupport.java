/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.execution;

import io.jmix.aitools.dataload.execution.JpqlDeclarationSupport.Declaration;
import io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.impl.QueryParamValuesManager;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.EntityOp;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.accesscontext.ReadEntityQueryContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Checks a validated data-load query against the current user's permissions for every entity it reads, and
 * returns the text to execute. Kept apart from execution so that any way of executing a generated query can
 * apply the same checks first.
 * <p>
 * An entity counts as read when the query declares it (root, join, subquery) or when a path passes through it
 * ({@code o.customer.name} reads the customer). The platform checks entity READ and applies row-level conditions
 * for the root entity only.
 */
@Component("aitls_JpqlAccessSupport")
public class JpqlAccessSupport {

    private static final Logger log = LoggerFactory.getLogger(JpqlAccessSupport.class);

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected JpqlDeclarationSupport declarationSupport;
    @Autowired
    protected QueryParamValuesManager queryParamValuesManager;

    /**
     * Checks the query and its parameters and returns the text to execute, with the row-level conditions of every
     * non-root entity woven in. A text the JPQL parser cannot read is returned unchanged, since executing it fails
     * on the same parser.
     *
     * @param jpql           validated query text
     * @param parameterNames names of the parameters passed with the query
     * @return the text to execute
     * @throws AccessDeniedException         if the current user may not read an entity the query reads
     * @throws JpqlAccessConstraintException if the query cannot be narrowed as the current user's permissions require
     */
    public String applyAccessConstraints(String jpql, Collection<String> parameterNames) {
        Preconditions.checkNotNullArgument(jpql, "jpql is null");
        Preconditions.checkNotNullArgument(parameterNames, "parameterNames is null");
        checkParameterNames(parameterNames);

        QueryParser parser;
        try {
            parser = queryTransformerFactory.parser(jpql);
            // The parser reads the text lazily: asking for the root is what tells a readable text.
            parser.getEntityName();
        } catch (RuntimeException e) {
            log.debug("[{}] cannot be parsed, so access checks are skipped: executing it fails the same way", jpql, e);
            return jpql;
        }

        QueryGraph graph;
        try {
            graph = readGraph(jpql, parser);
        } catch (JpqlAccessConstraintException e) {
            throw e;
        } catch (RuntimeException e) {
            // A readable text whose entities still cannot be told is refused: skipping the checks would let it
            // through unchecked.
            log.debug("The entities read by [{}] cannot be told", jpql, e);
            throw new JpqlAccessConstraintException("The entities this query reads cannot be determined, so it cannot "
                    + "be checked against the current user's permissions. Rewrite it in a simpler form, with plain "
                    + "joins and paths");
        }

        // Entity READ first: no other diagnostic may tell anything about an entity the user may not read.
        checkEntityReadPermitted(graph);
        checkSelectsValues(parser);

        String constrained = applyRowLevelConditions(jpql, graph);
        if (!constrained.equals(jpql)) {
            log.debug("Row-level conditions applied to [{}]: {}", jpql, constrained);
        }
        return constrained;
    }

    /**
     * Refuses query parameters whose values the application supplies itself ({@code current_user_*},
     * {@code session_*}, {@code current_locale}). Row-level conditions refer to them, and a value passed with
     * the query would take the place of the application's.
     *
     * @param parameterNames names of the parameters passed with the query
     * @throws JpqlAccessConstraintException if one of them is supplied by the application
     */
    protected void checkParameterNames(Collection<String> parameterNames) {
        for (String name : parameterNames) {
            if (queryParamValuesManager.supports(name)) {
                throw new JpqlAccessConstraintException(String.format(
                        "Parameter :%s is reserved: the application supplies its value. Give the parameter another "
                                + "name", name));
            }
        }
    }

    /**
     * Collects every entity the query reads, with the expressions that stand for it in the outer query and
     * whether it also occurs where a condition added to the outer query cannot reach it.
     *
     * @param jpql   query text
     * @param parser parser of the query
     * @return the query graph
     */
    protected QueryGraph readGraph(String jpql, QueryParser parser) {
        List<Declaration> declarations = declarationSupport.readDeclarations(jpql);

        Map<String, Integer> joinsPerPath = new HashMap<>();
        for (Declaration declaration : declarations) {
            String joinPath = declaration.joinPath();
            if (joinPath != null) {
                joinsPerPath.merge(lowerCase(joinPath), 1, Integer::sum);
            }
        }

        Map<String, EntityOccurrence> entities = new TreeMap<>();
        Set<String> outerVariables = new HashSet<>();
        for (Declaration declaration : declarations) {
            EntityOccurrence occurrence = occurrenceOf(entities, declaration.entityName());
            if (declaration.nested()) {
                occurrence.inner = true;
            } else {
                occurrence.outerTargets.put(declaration.variable(), declaration.nullCheck());
                outerVariables.add(lowerCase(declaration.variable()));
            }
        }

        Map<String, Integer> pathsPerPath = new HashMap<>();
        for (QueryParser.QueryPath path : parser.getQueryPaths()) {
            pathsPerPath.merge(lowerCase(path.getFullPath()), 1, Integer::sum);
        }
        for (QueryParser.QueryPath path : parser.getQueryPaths()) {
            boolean outer = outerVariables.contains(lowerCase(path.getVariableName()));
            addPathReachedEntities(path, outer, entities, pathsPerPath, joinsPerPath);
        }
        return new QueryGraph(parser.getEntityName(), parser.getEntityAlias(), entities);
    }

    protected void addPathReachedEntities(QueryParser.QueryPath path, boolean outer,
                                          Map<String, EntityOccurrence> entities,
                                          Map<String, Integer> pathsPerPath, Map<String, Integer> joinsPerPath) {
        if (!path.getFullPath().contains(".")) {
            return;
        }
        MetaClass metaClass = metadata.findClass(path.getEntityName());
        MetaPropertyPath propertyPath = metaClass != null ? metaClass.getPropertyPath(path.getPropertyPath()) : null;
        if (propertyPath == null) {
            return;
        }

        MetaProperty[] properties = propertyPath.getMetaProperties();
        StringBuilder prefix = new StringBuilder(path.getVariableName());
        for (int i = 0; i < properties.length; i++) {
            MetaProperty property = properties[i];
            prefix.append('.').append(property.getName());
            if (!property.getRange().isClass() || metadataTools.isJpaEmbeddable(property.getRange().asClass())) {
                continue;
            }

            boolean many = property.getRange().getCardinality().isMany();
            if (!many && i == properties.length - 1) {
                // A path ending in a to-one reference (`o.customer = c`, `o.customer is null`) reads the foreign
                // key of its owner, not the referenced entity; selecting it whole is refused separately.
                continue;
            }

            EntityOccurrence occurrence = occurrenceOf(entities, property.getRange().asClass().getName());
            if (many) {
                // A collection path is either a join declaration, whose alias is a target already, or a read of the
                // collection without an alias (size, member of, is empty), which no condition can narrow.
                String key = lowerCase(prefix.toString());
                if (pathsPerPath.getOrDefault(key, 0) > joinsPerPath.getOrDefault(key, 0)) {
                    occurrence.inner = true;
                }
            } else if (outer) {
                String target = prefix.toString();
                occurrence.outerTargets.putIfAbsent(target, metadataTools.isOwningSide(property) ? target : null);
            } else {
                occurrence.inner = true;
            }
        }
    }

    protected String lowerCase(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    protected void checkEntityReadPermitted(QueryGraph graph) {
        for (String entityName : graph.entities().keySet()) {
            MetaClass entity = metadata.findClass(entityName);
            if (entity == null) {
                // Unknown to the data model: validation rejects it, and executing it would fail anyway.
                continue;
            }
            CrudEntityContext entityContext = new CrudEntityContext(entity);
            accessManager.applyRegisteredConstraints(entityContext);
            if (!entityContext.isReadPermitted()) {
                throw new AccessDeniedException("entity", entity.getName(), EntityOp.READ.getId());
            }
        }
    }

    protected void checkSelectsValues(QueryParser parser) {
        List<String> entityExpressions = JpqlValidatorSupport.selectedEntityExpressions(parser, metadata);
        if (!entityExpressions.isEmpty()) {
            throw new JpqlAccessConstraintException(String.format(
                    "The query selects %s as a whole entity rather than as values. An entity is not returned as a "
                            + "value: select the attributes you need instead",
                    String.join(", ", entityExpressions)));
        }
    }

    /**
     * Returns the text to execute with the row-level conditions of every non-root entity applied. These are the
     * conditions the platform would apply to a query rooted at that entity.
     *
     * @param jpql  validated query text
     * @param graph graph of the query
     * @return the text to execute, unchanged when there is nothing to apply
     * @throws JpqlAccessConstraintException if an entity with conditions occurs where they cannot be applied, or a
     *                                       condition of a non-root entity carries a join
     */
    protected String applyRowLevelConditions(String jpql, QueryGraph graph) {
        QueryTransformer transformer = null;
        for (Map.Entry<String, EntityOccurrence> entry : graph.entities().entrySet()) {
            MetaClass entity = metadata.findClass(entry.getKey());
            if (entity == null) {
                continue;
            }
            List<RowLevelCondition> conditions = collectRowLevelConditions(entity);
            if (conditions.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, String> target : targetsOf(entity, entry.getValue(), conditions, graph).entrySet()) {
                for (RowLevelCondition condition : conditions) {
                    String where = condition.where();
                    if (where == null || where.isBlank()) {
                        continue;
                    }
                    if (transformer == null) {
                        transformer = queryTransformerFactory.transformer(jpql);
                    }
                    transformer.addWhere(nullSafe(target.getValue(),
                            where.replace(QueryTransformer.ALIAS_PLACEHOLDER, target.getKey())));
                }
            }
        }
        return transformer != null ? transformer.getResult() : jpql;
    }

    /**
     * Lets through a row that refers to no record, since an absent record carries no data. Absence is checked on
     * the owner's foreign key only, because a check on the joined variable would let an {@code on} clause probe a
     * hidden record.
     *
     * @param nullCheck path whose {@code is null} tells that no record is referred to, or {@code null} when absence
     *                  cannot be told that way (a collection, the inverse side, an entity join)
     * @param condition condition with the target in place of {@code {E}}
     * @return the condition to add to the {@code where}
     */
    protected String nullSafe(@Nullable String nullCheck, String condition) {
        return nullCheck != null
                ? String.format("(%s is null or (%s))", nullCheck, condition)
                : condition;
    }

    /**
     * Returns the expressions to apply the entity's row-level conditions to: every expression standing for it in
     * the outer query except the root alias, which the platform narrows itself.
     *
     * @param entity     entity the conditions belong to
     * @param occurrence where the entity occurs in the query
     * @param conditions the entity's row-level conditions, not empty
     * @param graph      graph of the query
     * @return target expressions, each with its null check or {@code null}; empty when the platform applies them all
     * @throws JpqlAccessConstraintException if the entity occurs where the conditions cannot be applied, or a
     *                                       condition to apply carries a join
     */
    protected Map<String, @Nullable String> targetsOf(MetaClass entity, EntityOccurrence occurrence,
                                    List<RowLevelCondition> conditions, QueryGraph graph) {
        if (occurrence.inner) {
            throw new JpqlAccessConstraintException(String.format(
                    "The query reads entity %s where its row-level conditions cannot be applied: in a subquery, "
                            + "or as a collection without an alias. Rewrite the query so that it reads %s "
                            + "through a join or a path in the main query",
                    entity.getName(), entity.getName()));
        }

        Map<String, @Nullable String> targets = new LinkedHashMap<>(occurrence.outerTargets);
        if (entity.getName().equals(graph.rootEntity()) && graph.rootAlias() != null) {
            // The platform applies the root's conditions, join and where alike, to the root alias.
            targets.remove(graph.rootAlias());
        }
        if (targets.isEmpty()) {
            return targets;
        }

        if (conditions.stream().anyMatch(condition -> !isBlank(condition.join()))) {
            // A join can only be added from the root alias (QueryTransformer#addJoinAndWhere re-bases it there),
            // so it would narrow another entity or name a path that does not exist.
            throw new JpqlAccessConstraintException(String.format(
                    "The query reads entity %s, whose row-level conditions join another entity. They can be "
                            + "applied only when %s is the entity the query selects from: rewrite the query that way",
                    entity.getName(), entity.getName()));
        }
        return targets;
    }

    /**
     * Returns the row-level conditions that the registered constraints add for the entity, as they would for a
     * query rooted at it.
     *
     * @param entity entity to collect the conditions of
     * @return the conditions with the {@code {E}} placeholder still in place; empty if none
     */
    protected List<RowLevelCondition> collectRowLevelConditions(MetaClass entity) {
        ConditionCollectingContext context = new ConditionCollectingContext(entity, queryTransformerFactory);
        accessManager.applyRegisteredConstraints(context);
        return context.getConditions();
    }

    protected boolean isBlank(@Nullable String clause) {
        return clause == null || clause.isBlank();
    }

    protected EntityOccurrence occurrenceOf(Map<String, EntityOccurrence> entities, String entityName) {
        return entities.computeIfAbsent(entityName, name -> new EntityOccurrence());
    }

    /**
     * Entities a query reads, keyed and ordered by entity name.
     *
     * @param rootEntity the entity the query selects from, whose row-level conditions the platform applies
     * @param rootAlias  the variable the query selects from
     * @param entities   every entity the query reads
     */
    protected record QueryGraph(String rootEntity, @Nullable String rootAlias,
                                Map<String, EntityOccurrence> entities) {
    }

    /**
     * Where one entity occurs in a query.
     */
    protected static class EntityOccurrence {

        /**
         * The entity's aliases and the path prefixes reaching it in the outer query, each mapped to its null check
         * or {@code null}.
         */
        protected final Map<String, @Nullable String> outerTargets = new LinkedHashMap<>();

        /**
         * Whether the entity also occurs where an outer condition cannot reach it: in a subquery, or as a
         * collection without an alias.
         */
        protected boolean inner;
    }

    /**
     * A row-level condition as a constraint added it.
     *
     * @param join  join clause, or {@code null}
     * @param where where clause, or {@code null}
     */
    protected record RowLevelCondition(@Nullable String join, @Nullable String where) {
    }

    /**
     * Row-level query context for a non-root entity. It records the conditions that constraints add instead of
     * rewriting a query, because no query is rooted at that entity.
     */
    protected static class ConditionCollectingContext extends ReadEntityQueryContext {

        protected final List<RowLevelCondition> conditions = new ArrayList<>();

        public ConditionCollectingContext(MetaClass entity, QueryTransformerFactory transformerFactory) {
            // No query: this context is only asked for conditions, never for a result query.
            super(null, entity, transformerFactory);
        }

        @Override
        public void addJoinAndWhere(@Nullable String join, @Nullable String where) {
            conditions.add(new RowLevelCondition(join, where));
        }

        public List<RowLevelCondition> getConditions() {
            return List.copyOf(conditions);
        }
    }
}
