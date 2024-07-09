import {Navigate, Route, Routes} from "react-router-dom";
import {Dashboard} from "../../../page/dashboard/Dashboard.tsx";
import {Page404} from "../../security/Page404.tsx";
import {ProcessListPage} from "../../../page/process/ProcessListPage.tsx";
import {TaskMasterDetailsPage} from "../../../page/task/TaskMasterDetailsPage.tsx";

export const AppRoutes = () => {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="dashboard"/>}/>
            <Route path="/dashboard" element={<Dashboard/>}/>
            <Route path="*" element={<Page404/>}/>
            <Route path="/processes" element={<ProcessListPage/>}/>
            <Route path="/tasks">
                <Route index={true} element={<TaskMasterDetailsPage/>}/>
            </Route>
        </Routes>
    );
};