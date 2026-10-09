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

import io.jmix.aitools.ExcludeFromAi;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Checks a validated data-load query against the current user's permissions for every entity it reads, and
 * returns the text to execute. Kept apart from execution so that any way of executing a generated query can
 * apply the same checks first.
 * <p>
 * An entity counts as read when the query declares it (root, join, subquery) or when a path passes through it
 * ({@code o.customer.name} reads the customer). The platform checks entity READ and applies row-level conditions
 * for the root entity only.
 * <p>
 * Every entity the query reads is also narrowed to the records the AI may see: records of a {@code @ExcludeFromAi}
 * subclass are records of its base entity too, and a query over the base would otherwise return them.
 * <p>
 * A {@code left join} is narrowed in its own {@code on} clause, so that a hidden record is joined as an absent one.
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
    protected JpqlLeftJoinSupport leftJoinSupport;
    @Autowired
    protected QueryParamValuesManager queryParamValuesManager;

    /**
     * Checks the query and its parameters and returns the text to execute, with the row-level conditions of every
     * non-root entity and the exclusion of {@code @ExcludeFromAi} subclasses woven in: into the {@code on} clause of
     * a left join, into the {@code where} otherwise. A text the JPQL parser cannot read is returned unchanged, since
     * executing it fails on the same parser.
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

        String withLeftJoins = applyLeftJoinConditions(jpql, graph);
        String withAppliedRowLevel = applyRowLevelConditions(withLeftJoins, graph);
        String constrained = applyExcludedSubtypeConditions(withAppliedRowLevel, graph);

        if (!constrained.equals(jpql)) {
            log.debug("Access conditions applied to [{}]: {}", jpql, constrained);
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
                if (declaration.leftJoin()) {
                    occurrence.leftJoinTargets.add(declaration.variable());
                } else {
                    occurrence.outerTargets.put(declaration.variable(), declaration.nullCheck());
                }
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
        if (outer && isIdOfReference(properties)) {
            // `d.parent.id` reads the referenced record's key: rewritten into a left join of its own, so that a
            // hidden record reads as an absent one instead of removing the row.
            MetaClass referenced = properties[0].getRange().asClass();
            Map<String, IdPathTarget> idPathTargets = occurrenceOf(entities, referenced.getName()).idPathTargets;
            String target = path.getVariableName() + "." + properties[0].getName();
            // The variable is matched case-insensitively when rewriting, so `O.customer.id` and `o.customer.id`
            // are one reference and get one join.
            if (idPathTargets.keySet().stream().noneMatch(target::equalsIgnoreCase)) {
                idPathTargets.put(target, new IdPathTarget(
                        metadataTools.isOwningSide(properties[0]) ? target : null, metaClass));
            }
            return;
        }
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

    protected boolean isIdOfReference(MetaProperty[] properties) {
        if (properties.length != 2) {
            return false;
        }
        MetaProperty reference = properties[0];
        if (!reference.getRange().isClass() || reference.getRange().getCardinality().isMany()
                || metadataTools.isJpaEmbeddable(reference.getRange().asClass())) {
            return false;
        }
        return properties[1].getName().equals(metadataTools.getPrimaryKeyName(reference.getRange().asClass()));
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
     * Narrows the entities joined by a {@code left join} in the join's own {@code on} clause, so that a hidden record
     * is joined as an absent one and the row stays. Covers row-level conditions and excluded subclasses alike.
     *
     * @param jpql  query text
     * @param graph graph of the query
     * @return the text with the conditions in the {@code on} clauses, unchanged when there is nothing to apply
     * @throws JpqlAccessConstraintException if a row-level condition to apply carries a join; if a navigating
     *                                       condition has to be wrapped into a subquery and the entity has no primary
     *                                       key; or if a {@code <ref>.<id>} path occurs in the {@code on} clause of
     *                                       the left join declaring its variable
     * @throws IllegalStateException         if the query text cannot be rewritten as its graph requires
     */
    protected String applyLeftJoinConditions(String jpql, QueryGraph graph) {
        JpqlLeftJoinSupport.Rewrite rewrite = null;
        for (Map.Entry<String, EntityOccurrence> entry : graph.entities().entrySet()) {
            MetaClass entity = metadata.findClass(entry.getKey());
            EntityOccurrence occurrence = entry.getValue();
            if (entity == null || !occurrence.hasOnTargets()) {
                continue;
            }
            List<RowLevelCondition> rowLevelConditions = collectRowLevelConditions(entity);
            Set<String> excludedSubtypes = excludedSubtypeNames(entity);
            if (rowLevelConditions.isEmpty() && excludedSubtypes.isEmpty()) {
                continue;
            }
            checkNoJoinClause(entity, rowLevelConditions);

            if (rewrite == null) {
                rewrite = leftJoinSupport.rewrite(jpql);
            }
            List<String> variables = new ArrayList<>(occurrence.leftJoinTargets);
            String primaryKey = metadataTools.getPrimaryKeyName(entity);
            if (primaryKey == null && !occurrence.idPathTargets.isEmpty()) {
                // A registered id path reads the entity's key (isIdOfReference), so the key exists; never leave the
                // path unconstrained if that ever changes.
                throw new IllegalStateException("Entity " + entity.getName() + " has no primary key, yet the query "
                        + "reads it through " + occurrence.idPathTargets.keySet());
            }
            LeftJoinConditions leftJoinConditions = classifyConditions(entity, rowLevelConditions, primaryKey);
            for (Map.Entry<String, IdPathTarget> idPath : occurrence.idPathTargets.entrySet()) {
                String referencePath = idPath.getKey();
                JpqlLeftJoinSupport.Rewrite.JoinedIdPath joined = rewrite.joinIdPath(referencePath, primaryKey);
                String variable = joined.variable();

                if (variable != null) {
                    variables.add(variable);
                }
                if (joined.selected()) {
                    rewrite.addWhereCondition(referenceExistsCondition(referencePath, idPath.getValue().owner(),
                            rewrite));
                }
                if (joined.leftInInnerJoinOn()) {
                    // An inner join drops a row without a visible record wherever it is narrowed: the path is
                    // narrowed in the `where` as a path prefix, by the passes that follow.
                    occurrence.outerTargets.putIfAbsent(referencePath, idPath.getValue().nullCheck());
                }
            }
            for (String variable : variables) {
                for (String condition : onConditions(entity, leftJoinConditions, excludedSubtypes, variable, rewrite)) {
                    rewrite.addOnCondition(variable, condition);
                }
            }
        }
        return rewrite != null ? rewrite.getResult() : jpql;
    }

    /**
     * Returns a condition keeping only the rows whose owner refers to a record through the reference, as the
     * inner join of a selected {@code <ref>.<id>} path does. It reads the owner through a subquery: a condition on
     * {@code <owner>.<ref>} itself would be merged with the left join added for the path and drop the rows whose
     * record is hidden.
     *
     * @param referencePath path from the owner variable to the reference, such as {@code o.customer}
     * @param owner         entity the owner variable ranges over
     * @param rewrite       rewrite of the query, used for a fresh variable name
     * @return the condition
     * @throws IllegalStateException if the owner entity has no primary key
     */
    protected String referenceExistsCondition(String referencePath, MetaClass owner,
                                              JpqlLeftJoinSupport.Rewrite rewrite) {
        String ownerKey = metadataTools.getPrimaryKeyName(owner);
        if (ownerKey == null) {
            throw new IllegalStateException("Entity " + owner.getName() + " has no primary key, so the rows reading "
                    + "a key through " + referencePath + " cannot be told");
        }

        int dot = referencePath.indexOf('.');
        String ownerVariable = referencePath.substring(0, dot);
        String reference = referencePath.substring(dot + 1);
        String variable = rewrite.newVariable();

        return String.format("%s.%s in (select %s.%s from %s %s where %s.%s is not null)",
                ownerVariable, ownerKey, variable, ownerKey, owner.getName(), variable, variable, reference);
    }

    /**
     * Sorts the entity's row-level conditions by how they go into a join's {@code on} clause: as they are, or
     * wrapped into a subquery on the entity because they navigate a reference of it, which an {@code on} clause
     * cannot do. Decided once per condition, not per variable it is applied to.
     *
     * @param entity             entity the conditions belong to
     * @param rowLevelConditions the entity's row-level conditions
     * @param primaryKey         name of the entity's primary key, or {@code null} if it has none
     * @return the conditions, with the {@code {E}} placeholder still in place
     * @throws JpqlAccessConstraintException if a condition navigates and the entity has no primary key to wrap it
     *                                       into a subquery by
     */
    protected LeftJoinConditions classifyConditions(MetaClass entity, List<RowLevelCondition> rowLevelConditions,
                                                    @Nullable String primaryKey) {
        List<String> plain = new ArrayList<>();
        List<String> navigating = new ArrayList<>();
        for (RowLevelCondition condition : rowLevelConditions) {
            String where = condition.where();
            if (where == null || where.isBlank()) {
                continue;
            }
            if (!navigatesReference(entity, where)) {
                plain.add(where);
            } else if (primaryKey == null) {
                throw new JpqlAccessConstraintException(String.format(
                        "The query reads entity %s through a left join, and its row-level conditions cannot be "
                                + "applied there: the entity has no primary key. Rewrite the query so that it "
                                + "reads %s as the entity the query selects from",
                        entity.getName(), entity.getName()));
            } else {
                navigating.add(where);
            }
        }
        return new LeftJoinConditions(plain, navigating);
    }

    /**
     * Returns the conditions narrowing one variable of the entity in its join's {@code on} clause.
     *
     * @param entity           entity the variable ranges over
     * @param conditions       the entity's row-level conditions, sorted by {@link #classifyConditions}
     * @param excludedSubtypes names of the entity's {@code @ExcludeFromAi} descendants
     * @param variable         variable to narrow
     * @param rewrite          rewrite of the query, used for fresh variable names
     * @return the conditions, with the variable in place of {@code {E}}
     */
    protected List<String> onConditions(MetaClass entity, LeftJoinConditions conditions, Set<String> excludedSubtypes,
                                        String variable, JpqlLeftJoinSupport.Rewrite rewrite) {
        List<String> result = new ArrayList<>();
        for (String where : conditions.plain()) {
            result.add(where.replace(QueryTransformer.ALIAS_PLACEHOLDER, variable));
        }
        if (!conditions.navigating().isEmpty()) {
            String primaryKey = metadataTools.getPrimaryKeyName(entity);
            for (String where : conditions.navigating()) {
                String subqueryVariable = rewrite.newVariable();
                result.add(String.format("%s.%s in (select %s.%s from %s %s where %s)",
                        variable, primaryKey, subqueryVariable, primaryKey, entity.getName(), subqueryVariable,
                        where.replace(QueryTransformer.ALIAS_PLACEHOLDER, subqueryVariable)));
            }
        }
        if (!excludedSubtypes.isEmpty()) {
            result.add(excludedSubtypeCondition(variable, excludedSubtypes));
        }
        return result;
    }

    /**
     * Tells whether a row-level condition reaches beyond the entity's own columns: through a reference to another
     * entity, or into a collection. A condition that cannot be read is treated as navigating.
     *
     * @param entity entity the condition belongs to
     * @param where  the condition with the {@code {E}} placeholder
     * @return whether the condition navigates
     */
    protected boolean navigatesReference(MetaClass entity, String where) {
        String variable = "aitlsNavigationCheck";
        try {
            QueryParser parser = queryTransformerFactory.parser(String.format("select %s from %s %s where %s",
                    variable, entity.getName(), variable, where.replace(QueryTransformer.ALIAS_PLACEHOLDER, variable)));
            for (QueryParser.QueryPath path : parser.getQueryPaths()) {
                if (!variable.equalsIgnoreCase(path.getVariableName()) || !path.getFullPath().contains(".")) {
                    continue;
                }
                MetaPropertyPath propertyPath = entity.getPropertyPath(path.getPropertyPath());
                if (propertyPath == null) {
                    return true;
                }
                MetaProperty[] properties = propertyPath.getMetaProperties();
                for (int i = 0; i < properties.length; i++) {
                    MetaProperty property = properties[i];
                    if (!property.getRange().isClass() || metadataTools.isJpaEmbeddable(property.getRange().asClass())) {
                        continue;
                    }
                    // A to-one reference at the end of the path reads the foreign key only.
                    if (property.getRange().getCardinality().isMany() || i < properties.length - 1) {
                        return true;
                    }
                }
            }
            return false;
        } catch (RuntimeException e) {
            log.debug("Cannot read row-level condition [{}] of {}, treating it as navigating", where, entity.getName(), e);
            return true;
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
     * Keeps the records of {@code @ExcludeFromAi} subclasses out of the result: such a subclass is closed to the AI,
     * yet its records are records of every visible ancestor. Unlike row-level conditions, this applies to the root
     * alias too, since the platform knows nothing of the annotation.
     *
     * @param jpql  query text
     * @param graph graph of the query
     * @return the text to execute, unchanged when no entity the query reads has an excluded subclass
     * @throws JpqlAccessConstraintException if an entity with an excluded subclass occurs where a condition added to
     *                                       the outer query cannot reach it
     */
    protected String applyExcludedSubtypeConditions(String jpql, QueryGraph graph) {
        QueryTransformer transformer = null;
        for (Map.Entry<String, EntityOccurrence> entry : graph.entities().entrySet()) {
            MetaClass entity = metadata.findClass(entry.getKey());
            if (entity == null) {
                continue;
            }
            Set<String> excludedSubtypes = excludedSubtypeNames(entity);
            if (excludedSubtypes.isEmpty()) {
                continue;
            }

            EntityOccurrence occurrence = entry.getValue();
            if (occurrence.inner) {
                // The message names the entity the query reads, never the excluded subclass.
                throw new JpqlAccessConstraintException(String.format(
                        "The query reads entity %s where it cannot be narrowed to the records available to the AI: "
                                + "in a subquery, or as a collection without an alias. Rewrite the query so that it "
                                + "reads %s through a join or a path in the main query",
                        entity.getName(), entity.getName()));
            }
            for (Map.Entry<String, @Nullable String> target : occurrence.outerTargets.entrySet()) {
                if (transformer == null) {
                    transformer = queryTransformerFactory.transformer(jpql);
                }
                transformer.addWhere(nullSafe(target.getValue(),
                        excludedSubtypeCondition(target.getKey(), excludedSubtypes)));
            }
        }
        return transformer != null ? transformer.getResult() : jpql;
    }

    protected String excludedSubtypeCondition(String expression, Set<String> excludedSubtypes) {
        // The JPQL parser takes no entity type literal in an IN list, hence one comparison per subclass.
        return excludedSubtypes.stream()
                .map(subtype -> String.format("type(%s) <> %s", expression, subtype))
                .collect(Collectors.joining(" and "));
    }

    protected Set<String> excludedSubtypeNames(MetaClass entity) {
        Set<String> names = new TreeSet<>();
        for (MetaClass descendant : entity.getDescendants()) {
            if (descendant.getAnnotations().containsKey(ExcludeFromAi.class.getName())) {
                names.add(descendant.getName());
            }
        }
        return names;
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

        checkNoJoinClause(entity, conditions);
        return targets;
    }

    protected void checkNoJoinClause(MetaClass entity, List<RowLevelCondition> conditions) {
        if (conditions.stream().anyMatch(condition -> !isBlank(condition.join()))) {
            // A join can only be added from the root alias (QueryTransformer#addJoinAndWhere re-bases it there),
            // so it would narrow another entity or name a path that does not exist.
            throw new JpqlAccessConstraintException(String.format(
                    "The query reads entity %s, whose row-level conditions join another entity. They can be "
                            + "applied only when %s is the entity the query selects from: rewrite the query that way",
                    entity.getName(), entity.getName()));
        }
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
         * or {@code null}. Their conditions go to the outer {@code where}.
         */
        protected final Map<String, @Nullable String> outerTargets = new LinkedHashMap<>();

        /**
         * Variables declared for the entity by a {@code left join} of the outer query. Their conditions go to the
         * join's own {@code on}, so that a hidden record is joined as an absent one.
         */
        protected final Set<String> leftJoinTargets = new LinkedHashSet<>();

        /**
         * Outer reference paths whose primary key the query reads ({@code d.parent} for {@code d.parent.id}). Each is
         * rewritten into a left join of its own.
         */
        protected final Map<String, IdPathTarget> idPathTargets = new LinkedHashMap<>();

        /**
         * Whether the entity also occurs where an outer condition cannot reach it: in a subquery, or as a
         * collection without an alias.
         */
        protected boolean inner;

        protected boolean hasOnTargets() {
            return !leftJoinTargets.isEmpty() || !idPathTargets.isEmpty();
        }
    }

    /**
     * A reference path whose primary key the query reads.
     *
     * @param nullCheck the path whose {@code is null} tells that no record is referred to, or {@code null} when the
     *                  reference is not the owning side
     * @param owner     entity the path's variable ranges over
     */
    protected record IdPathTarget(@Nullable String nullCheck, MetaClass owner) {
    }

    /**
     * An entity's row-level conditions sorted by how they go into a join's {@code on} clause.
     *
     * @param plain      conditions added as they are
     * @param navigating conditions wrapped into a subquery on the entity, since they navigate a reference of it
     */
    protected record LeftJoinConditions(List<String> plain, List<String> navigating) {
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
