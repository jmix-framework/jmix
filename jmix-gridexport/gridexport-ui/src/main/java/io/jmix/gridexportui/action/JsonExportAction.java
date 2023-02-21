package io.jmix.gridexportui.action;

import io.jmix.ui.action.ActionType;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.gridexportui.exporter.json.JsonExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Action for export table content as JSON
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 */
@StudioAction(target = "io.jmix.ui.component.ListComponent", description = "Export selected entities to JSON")
@ActionType(JsonExportAction.ID)
public class JsonExportAction extends ExportAction {

    @Autowired
    protected Icons icons;

    public static final String ID = "jsonExport";

    public JsonExportAction(String id) {
        this(id, null);
    }

    public JsonExportAction() {
        this(ID);
    }

    public JsonExportAction(String id, String shortcut) {
        super(id, shortcut);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        withExporter(JsonExporter.class);
    }

    @Override
    public String getIcon() {
        return icons.get(JmixIcon.CODE);
    }
}