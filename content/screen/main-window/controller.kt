package ${packageName}

import com.haulmont.cuba.web.app.mainwindow.AppMainWindow
<%if (classComment) {%>
${classComment}<%}%>
class ${controllerName} : AppMainWindow()