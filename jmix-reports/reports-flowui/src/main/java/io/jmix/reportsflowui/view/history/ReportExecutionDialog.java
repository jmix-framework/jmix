/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsflowui.view.history;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportFilter;
import io.jmix.reports.ReportLoadContext;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reportsflowui.helper.GridSortHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

@ViewController("report_ReportExecutionDialogView")
@ViewDescriptor("report-execution-dialog-view.xml")
@LookupComponent("reportsDataGrid")
@DialogMode(width = "80em", resizable = true)
public class ReportExecutionDialog extends StandardListView<Report> {

    @ViewComponent
    protected TypedTextField<String> filterName;
    @ViewComponent
    protected TypedTextField<String> filterCode;
    @ViewComponent
    protected EntityComboBox<ReportGroup> filterGroup;
    @ViewComponent
    protected TypedDatePicker<Date> filterUpdatedDate;

    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected GridSortHelper gridSortHelper;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected DataGrid<Report> reportsDataGrid;
    @ViewComponent
    protected CollectionLoader<Report> reportsDl;

    protected MetaClass metaClassParameter;
    protected String screenParameter;

    @Subscribe
    public void onInit(final InitEvent event) {
        filterCode.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        filterName.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        filterGroup.addValueChangeListener(e -> onFilterFieldValueChange());
        filterUpdatedDate.addTypedValueChangeListener(e -> onFilterFieldValueChange());
    }

    @Supply(to = "reportsDataGrid.name", subject = "renderer")
    protected Renderer<Report> reportsDataGridNameRenderer() {
        return new TextRenderer<>(metadataTools::getInstanceName);
    }

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    protected List<Report> reportsDlLoadDelegate(LoadContext<Report> ignored) {
        ReportFilter filter = createFilter();

        Sort sort = getReportGridSort();
        ReportLoadContext context = new ReportLoadContext(filter, sort);
        List<Report> items = reportRepository.loadList(context);
        return items;
    }

    protected ReportFilter createFilter() {
        ReportFilter filter = new ReportFilter();
        // ui filters
        filter.setNameContains(filterName.getTypedValue());
        filter.setCodeContains(filterCode.getTypedValue());
        filter.setGroup(filterGroup.getValue());
        filter.setUpdatedAfter(filterUpdatedDate.getTypedValue());
        // access filters
        filter.setViewId(screenParameter);
        filter.setUser(currentUserSubstitution.getEffectiveUser());
        filter.setInputValueMetaClass(metaClassParameter);
        filter.setSystem(false);
        return filter;
    }

    protected Sort getReportGridSort() {
        return gridSortHelper.convertSortOrders(
                reportsDataGrid.getSortOrder(),
                Map.of("name", ReportLoadContext.LOCALIZED_NAME_SORT_KEY) // custom cell renderer
        );
    }

    protected void onFilterFieldValueChange() {
        reportsDl.load();
    }    

    public void setMetaClassParameter(MetaClass metaClassParameter) {
        this.metaClassParameter = metaClassParameter;
    }

    public void setScreenParameter(String screenParameter) {
        this.screenParameter = screenParameter;
    }
}
