package io.jmix.flowui.action.list;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.flowui.action.SecurityConstraintAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;

/**
 * Action that changes its {@code enabled} property depending on a selected item in a bound {@link Grid}.
 */
public abstract class SecuredListDataComponentAction<A extends SecuredListDataComponentAction<A, E>, E>
        extends ListDataComponentAction<A, E>
        implements SecurityConstraintAction {

    protected EntityOp constraintEntityOp;
    protected AccessManager accessManager;
    protected Metadata metadata;
    protected ApplicationContext applicationContext;

    public SecuredListDataComponentAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    protected void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && target != null
                && target.getSingleSelectedItem() != null;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null) {
            return false;
        }

        E singleSelected = target.getSingleSelectedItem();
        if (singleSelected == null) {
            return false;
        }

        if (constraintEntityOp != null) {
            MetaClass metaClass = metadata.getClass(singleSelected.getClass());
            InMemoryCrudEntityContext context = new InMemoryCrudEntityContext(metaClass, applicationContext);
            accessManager.applyRegisteredConstraints(context);

            if (constraintEntityOp == EntityOp.CREATE) {
                return context.isCreatePermitted(singleSelected);
            } else if (constraintEntityOp == EntityOp.READ) {
                return context.isReadPermitted(singleSelected);
            } else if (constraintEntityOp == EntityOp.UPDATE) {
                return context.isUpdatePermitted(singleSelected);
            } else if (constraintEntityOp == EntityOp.DELETE) {
                return context.isDeletePermitted(singleSelected);
            } else {
                return false;
            }
        }

        return super.isPermitted();
    }

    @Override
    public void setConstraintEntityOp(@Nullable EntityOp entityOp) {
        this.constraintEntityOp = entityOp;
    }

    @Nullable
    @Override
    public EntityOp getConstraintEntityOp() {
        return constraintEntityOp;
    }

    @SuppressWarnings("unchecked")
    public A withConstraintEntityOp(@Nullable EntityOp entityOp) {
        setConstraintEntityOp(entityOp);
        return ((A) this);
    }
}
