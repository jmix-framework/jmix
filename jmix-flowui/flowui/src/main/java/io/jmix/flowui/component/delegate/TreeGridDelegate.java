package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.grid.GridDataItems;
import io.jmix.flowui.data.grid.TreeGridDataItems;
import io.jmix.flowui.component.grid.TreeDataGrid;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("flowui_TreeGridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TreeGridDelegate<E> extends AbstractGridDelegate<TreeDataGrid<E>, E> {

    public TreeGridDelegate(TreeDataGrid<E> component) {
        super(component);
    }

    @Override
    protected boolean isSupportedDataItems(GridDataItems<E> gridDataItems) {
        return gridDataItems instanceof TreeGridDataItems;
    }

    @Override
    protected void setupEmptyDataProvider() {
        component.setDataProvider(new TreeDataProvider<>(new TreeData<>()));
    }

    @Override
    protected void setupAutowiredColumns(GridDataItems<E> gridDataItems) {
        Collection<MetaPropertyPath> paths = getAutowiredProperties(gridDataItems);

        Grid.Column<E> hierarchyColumn = null;
        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany()
                    && !metadataTools.isSystem(property)) {

                if (hierarchyColumn != null) {
                    addColumnInternal(metaPropertyPath);
                } else {
                    // init first column as hierarchy column
                    hierarchyColumn = addHierarchyColumnInternal(metaPropertyPath);
                }
            }
        }
    }

    protected Grid.Column<E> addHierarchyColumnInternal(MetaPropertyPath metaPropertyPath) {
        ValueProvider<E, ?> valueProvider = getValueProvider(metaPropertyPath);

        Grid.Column<E> column = component.addHierarchyColumn(valueProvider);

        initColumn(column, metaPropertyPath);

        return column;
    }
}
