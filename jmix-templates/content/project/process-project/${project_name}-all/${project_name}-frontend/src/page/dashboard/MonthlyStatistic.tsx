import {TaskExecutionStatistics} from "../../core/types.ts";
import {Statistic} from "antd";
import {FileDoneOutlined} from "@ant-design/icons";
import dayjs from "dayjs";

export interface MonthlyStatisticProps {
    data?: TaskExecutionStatistics
    loading: boolean
}

export const MonthlyStatistic = ({data, loading}: MonthlyStatisticProps) => {
    const monthlyCompletedTasksCount = data?.completedTasksCount || 0;
    const monthlyTotalCount = data?.totalTasks || 0;
    const monthName = dayjs().format("MMMM");
    return (
        <>
            <Statistic
                title={`Completed (\${monthName})`}
                loading={loading}
                value={`\${monthlyCompletedTasksCount}/\${monthlyTotalCount}`}
                prefix={<FileDoneOutlined/>}
            />
        </>
    );
};