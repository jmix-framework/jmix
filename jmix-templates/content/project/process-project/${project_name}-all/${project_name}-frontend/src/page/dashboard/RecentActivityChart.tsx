import {Card, Flex, theme} from "antd";
import {TaskExecutionPeriodStatistics} from "../../core/types.ts";
import dayjs from "dayjs";
import {BarDatum, BarTooltipProps, ComputedDatum, ResponsiveBar} from "@nivo/bar";
import Text from "antd/es/typography/Text";
import useBreakpoint from "antd/es/grid/hooks/useBreakpoint";
import {renderDate} from "../../core/format/renderDate.ts";
import {useCallback} from "react";
import "./RecentActivityChart.css";

export interface RecentActivityChartProps {
    data: TaskExecutionPeriodStatistics
}

const COMPLETED_TASKS_GROUP_ID = "Completed tasks"
const CREATED_TASKS_GROUP_ID = "Created tasks";


export const RecentActivityChart = ({
                                        data: {
                                            items,
                                            totalTasks,
                                            completedTasksCount
                                        }
                                    }: RecentActivityChartProps) => {
    const {
        token: {
            fontSize,
            fontFamily,
            colorPrimaryBgHover,
            fontSizeSM,
            colorPrimaryHover
        },
    } = theme.useToken();

    const {xs} = useBreakpoint();

    const tasksBarChartTheme = {
        text: {
            fontSize: xs ? fontSizeSM : fontSize,
            fontFamily: fontFamily
        }
    };

    const legendData = [{
        id: COMPLETED_TASKS_GROUP_ID,
        label: `\${COMPLETED_TASKS_GROUP_ID} (\${completedTasksCount})`,
        color: colorPrimaryHover
    },
        {
            id: CREATED_TASKS_GROUP_ID,
            label: `\${CREATED_TASKS_GROUP_ID} (\${totalTasks})`,
            color: colorPrimaryBgHover
        }
    ];

    const barData: BarDatum[] = items.map(value => {
        const barDataItem: BarDatum = {};
        barDataItem.date = value.date;
        barDataItem[COMPLETED_TASKS_GROUP_ID] = value.completedTasksCount;
        barDataItem[CREATED_TASKS_GROUP_ID] = value.totalTasks;

        return barDataItem;
    });

    const barColors = useCallback((data: ComputedDatum<BarDatum>) => {
        if (data.id === COMPLETED_TASKS_GROUP_ID) {
            return colorPrimaryHover;
        }
        return colorPrimaryBgHover;
    }, [colorPrimaryHover, colorPrimaryBgHover]);


    const noData = totalTasks === 0 && completedTasksCount === 0;

    return (
        <>
            <div className="recent-activity__chart-root-container">
                <ResponsiveBar
                    tooltip={props => <TasksTooltip {...props}/>}
                    data={barData}
                    keys={[COMPLETED_TASKS_GROUP_ID, CREATED_TASKS_GROUP_ID]}
                    theme={tasksBarChartTheme}
                    indexBy="date"
                    margin={{top: 60, right: 40, bottom: 40, left: 40}}
                    padding={0.3}
                    groupMode="grouped"
                    valueScale={{type: "linear"}}
                    indexScale={{type: "band"}}
                    colors={barColors}
                    axisBottom={{
                        tickRotation: xs ? 45 : 0,
                        format: value => dayjs(value).format("MMM DD")
                    }}
                    minValue={noData ? 0 : "auto"}
                    maxValue={noData ? 1 : "auto"}
                    axisLeft={{
                        tickValues: noData ? 1 : 7,
                    }}
                    labelSkipWidth={12}
                    labelSkipHeight={12}
                    legends={[
                        {
                            dataFrom: "keys",
                            anchor: "top-left",
                            direction: xs ? "column" : "row",
                            translateY: xs ? -60 : -50,
                            itemWidth: xs ? 100 : 180,
                            itemHeight: 20,
                            itemDirection: "left-to-right",
                            itemOpacity: 0.85,
                            symbolSize: xs ? 10 : 20,
                            data: legendData
                        }
                    ]}
                />
            </div>
        </>
    );
};


export const TasksTooltip = (props: BarTooltipProps<BarDatum>) => {
    return (
        <>
            <Card styles={{body: {padding: "1em"}}}>
                <Flex vertical={true} gap={5}>
                    <Text strong={true}>{props.id}</Text>
                    <Flex gap={5}>
                        <Text strong>Date:</Text>
                        <Text>{renderDate(props.indexValue as string)}</Text>
                    </Flex>
                    <Flex gap={5}>
                        <Text strong>Count:</Text>
                        <Text>{props.value}</Text>
                    </Flex>
                </Flex>
            </Card>
        </>
    );
};
