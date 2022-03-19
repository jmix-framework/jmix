package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Calendar;
import io.jmix.ui.xml.layout.loader.CalendarLoader;
import org.dom4j.Element;

public class CubaCalendarLoader extends CalendarLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(com.haulmont.cuba.gui.components.Calendar.NAME);
        loadId(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void loadDataContainer(Calendar component, Element element) {
        String datasource = element.attributeValue("datasource");

        ComponentLoaderContext loaderContext = (ComponentLoaderContext) getComponentContext();

        CollectionDatasource ds = (CollectionDatasource) loaderContext.getDsContext().get(datasource);
        if (ds == null) {
            throw new GuiDevelopmentException(String.format("Datasource '%s' is not defined", datasource),
                    getContext(), "Component ID", component.getId());
        }

        ((com.haulmont.cuba.gui.components.Calendar) component).setDatasource(ds);
    }
}
