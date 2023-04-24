<%
def imageType = version.startsWith("7") ? "Image" : "Embedded"
%>package ${packageName};

import com.haulmont.cuba.gui.components.AbstractMainWindow;
import com.haulmont.cuba.gui.components.${imageType};
import com.haulmont.cuba.gui.components.mainwindow.FtsField;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

<%if (classComment) {%>
${classComment}<%}%>
public class ${controllerName} extends AbstractMainWindow {
    @Autowired
    private FtsField ftsField;

    @Autowired
    private ${imageType} logoImage;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);

        initLayoutAnalyzerContextMenu(logoImage);
        initLogoImage(logoImage);
        initFtsField(ftsField);
    }
}