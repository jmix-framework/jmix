package io.jmix.data.impl.context;


import com.google.common.base.Strings;
import io.jmix.core.QueryTransformer;
import io.jmix.core.QueryTransformerFactory;
import io.jmix.core.common.util.StringHelper;
import io.jmix.core.context.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.impl.JmixQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Modifies the query depending on current security constraints.
 */
public class ReadEntityQueryContext implements AccessContext {

    protected final QueryTransformerFactory queryTransformerFactory;

    protected final JmixQuery<?> originalQuery;
    protected final MetaClass entityClass;
    protected final boolean singleResult;
    protected List<Condition> conditions;
    protected Function<String, Object> queryParamsProvider;

    private static final Logger log = LoggerFactory.getLogger(ReadEntityQueryContext.class);

    protected static class Condition {
        final String where;
        final String join;

        public Condition(String where, String join) {
            this.where = where;
            this.join = join;
        }
    }

    public ReadEntityQueryContext(JmixQuery<?> originalQuery, MetaClass entityClass, QueryTransformerFactory transformerFactory) {
        this.originalQuery = originalQuery;
        this.entityClass = entityClass;
        this.queryTransformerFactory = transformerFactory;
        this.singleResult = false;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public void addJoinAndWhere(String where, String join) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        conditions.add(new Condition(where, join));
    }

    public void setQueryParamsProvider(Function<String, Object> queryParamsProvider) {
        this.queryParamsProvider = queryParamsProvider;
    }

    public JmixQuery<?> getResultQuery() {
        buildQuery();
        return originalQuery;
    }

    protected void buildQuery() {
        if (conditions != null) {
            QueryTransformer transformer = queryTransformerFactory.transformer(originalQuery.getQueryString());
            boolean hasJoins = false;

            //TODO: handle exception and throw RLS exception
            //        } catch (JpqlSyntaxException e) {
//            log.error("Syntax errors found in constraint's JPQL expressions. Entity [{}]. Constraint ID [{}].",
//                    entityName, constraint.getId(), e);
//
//            throw new RowLevelSecurityException(
//                    "Syntax errors found in constraint's JPQL expressions. Please see the logs.", entityName);
//        } catch (Exception e) {
//            log.error("An error occurred when applying security constraint. Entity [{}]. Constraint ID [{}].",
//                    entityName, constraint.getId(), e);
//
//            throw new RowLevelSecurityException(
//                    "An error occurred when applying security constraint. Please see the logs.", entityName);
//        }
            for (Condition condition : conditions) {
                if (!Strings.isNullOrEmpty(condition.join)) {
                    hasJoins = true;
                    transformer.addJoinAndWhere(condition.join, condition.where);
                } else {
                    transformer.addWhere(condition.where);
                }
            }
            if (hasJoins && singleResult) {
                transformer.addDistinct();
            }
            originalQuery.setQueryString(transformer.getResult());

            if (queryParamsProvider != null) {
                for (String param : transformer.getAddedParams()) {
                    originalQuery.setParameter(param, queryParamsProvider.apply(param));
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Constraints applied: {}", printQuery(originalQuery.getQueryString()));
            }
        }
    }

    protected static String printQuery(String query) {
        return query == null ? null : StringHelper.removeExtraSpaces(query.replace('\n', ' '));
    }
}
