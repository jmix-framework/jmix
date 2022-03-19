<%
def imageType = version.startsWith("7") ? "Image" : "Embedded"
%>package ${packageName}

import com.haulmont.cuba.gui.components.AbstractMainWindow
import com.haulmont.cuba.gui.components.${imageType}
import com.haulmont.cuba.gui.components.mainwindow.FtsField
import com.haulmont.cuba.gui.components.mainwindow.SideMenu

import org.springframework.beans.factory.annotation.Autowired
import kotlin.Map

<%if (classComment) {%>
${classComment}<%}%>
class ${controllerName} : AbstractMainWindow() {
    @Autowired
    private lateinit var ftsField: FtsField

    @Autowired
    private lateinit var logoImage: ${imageType}

    @Autowired
    private lateinit var sideMenu: SideMenu

    @Override
    fun init(params: Map<String, Any?>) {
        super.init(params)

        sideMenu.requestFocus()

        initLayoutAnalyzerContextMenu(logoImage)
        initLogoImage(logoImage)
        initFtsField(ftsField)
    }
}