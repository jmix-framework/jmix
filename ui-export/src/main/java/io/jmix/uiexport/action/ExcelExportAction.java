package io.jmix.uiexport.action;

import io.jmix.ui.action.ActionType;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.uiexport.exporter.excel.ExcelExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Action for export table content in XLSX or XLS formats
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 */
@StudioAction(target = "io.jmix.ui.component.ListComponent", description = "Export selected entities to Excel")
@ActionType(ExcelExportAction.ID)
public class ExcelExportAction extends ExportAction {

    public static final String ID = "excelExport";

    @Autowired
    protected Icons icons;

    public ExcelExportAction(String id) {
        this(id, null);
    }

    public ExcelExportAction() {
        this(ID);
    }

    public ExcelExportAction(String id, String shortcut) {
        super(id, shortcut);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        withExporter(ExcelExporter.class);
    }

    @Override
    public String getIcon() {
        return icons.get(JmixIcon.EXCEL_ACTION);
    }
}