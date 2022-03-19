package ${packageName}

import com.haulmont.cuba.web.app.loginwindow.AppLoginWindow
<%if (classComment) {%>
${classComment}<%}%>
class ${controllerName} : AppLoginWindow()