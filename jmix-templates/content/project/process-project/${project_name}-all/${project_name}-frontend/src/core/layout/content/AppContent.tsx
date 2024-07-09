import {AppRoutes} from "../routes/AppRoutes.tsx";
import {AppFooter} from "../footer/AppFooter.tsx";
import {useSearchParams} from "react-router-dom";
import {getTaskParam} from "../../searchParamsUtils.ts";
import {Flex} from "antd";
import "./AppContent.css";

export const AppContent = () => {
    const [searchParams] = useSearchParams();
    const taskId = getTaskParam(searchParams);
    return (
        <>
            <Flex vertical={true} className="main-content__app_content"
                  style={{paddingBottom: taskId ? 0 : "2.5em"}}>
                <AppRoutes/>
                {!taskId && <AppFooter/>}
            </Flex>
        </>
    );
};