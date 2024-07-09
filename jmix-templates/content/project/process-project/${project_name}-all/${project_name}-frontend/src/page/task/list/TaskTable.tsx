import {
    deleteSortSearchParams,
    getPageDataParams,
    getSortParams,
    getTaskParam,
    setPageDataParams,
    setSortParams
} from "../../../core/searchParamsUtils.ts";
import {Table, TablePaginationConfig} from "antd";
import {ColumnsType} from "antd/es/table";
import {PageData, TaskTableSortData, UserTask} from "../../../core/types.ts";
import Text from "antd/es/typography/Text";
import {
    getProcessDefinitionRecordRepresentation
} from "../../../core/record-representation/getProcessDefinitionRecordRepresentation.ts";
import {renderDateTime} from "../../../core/format/renderDateTime.ts";
import {TaskDueDate} from "../TaskDueDate.tsx";
import {TaskPriority} from "../TaskPriority.tsx";
import Button from "antd/es/button";
import {FormOutlined} from "@ant-design/icons";
import {useCallback} from "react";
import {SorterResult, TableCurrentDataSource} from "antd/es/table/interface";
import {useSearchParams} from "react-router-dom";
import {parseSortOrder} from "../../../core/format/parseSortOrder.ts";
import "./TaskTable.css";

interface TaskTableProps {
    totalElements?: number,
    data?: UserTask[],
    loading: boolean
    handleTaskSelected: (id: string) => void
    handlePageDataChange: (pageData: PageData) => void
    handleSortChange: (sort?: TaskTableSortData) => void
}

export const TaskTable = ({
                              totalElements,
                              data,
                              loading,
                              handleTaskSelected,
                              handlePageDataChange,
                              handleSortChange
                          }: TaskTableProps) => {
    const [searchParams, setSearchParams] = useSearchParams();
    const selectedTaskId = getTaskParam(searchParams);
    const {page, size}: PageData = getPageDataParams(searchParams) || {page: 1, size: 10};
    const taskTableSortData: TaskTableSortData | undefined = getSortParams(searchParams);
    const tableOrder = parseSortOrder(taskTableSortData?.order);

    const columns: ColumnsType<UserTask> = [
        {
            title: "Name",
            dataIndex: "name",
            key: "name",
            sorter: true,
            sortOrder: taskTableSortData?.property === "name" ? tableOrder : undefined
        },
        {
            title: "Process",
            dataIndex: "processDefinition",
            key: "processDefinition",
            render: (_, {processDefinition}) =>
                <Text>{getProcessDefinitionRecordRepresentation(processDefinition)}</Text>,
            responsive: ["md"],
        },
        {
            title: "Creation date",
            dataIndex: "createDate",
            key: "createDate",
            render: (_, {createDate}) => <Text>{renderDateTime(createDate)}</Text>,
            sorter: true,
            sortOrder: taskTableSortData?.property === "createDate" ? tableOrder : undefined
        },
        {
            title: "Due date",
            dataIndex: "dueDate",
            key: "dueDate",
            render: (_, {dueDate}) => <TaskDueDate value={dueDate}/>,
            responsive: ["xl"],
            sorter: true,
            sortOrder: taskTableSortData?.property === "dueDate" ? tableOrder : undefined
        },
        {
            title: "Priority",
            key: "priority",
            dataIndex: "priority",
            render: (_, {priority}) => <TaskPriority value={priority}/>,
            responsive: ["xl"],
            sorter: true,
            sortOrder: taskTableSortData?.property === "priority" ? tableOrder : undefined
        },
        {
            render: (_, {id}) => (
                <Button type="link" size="large"
                        onClick={(event) => onEditButtonClick(event, id)}>
                    <FormOutlined/>
                </Button>
            ),
        }
    ];

    const onEditButtonClick = useCallback((event: React.MouseEvent<HTMLElement>, id: string) => {
        event.stopPropagation();
        handleTaskSelected(id);
    }, [handleTaskSelected]);

    const onTaskRowClick = useCallback((task: UserTask) => {
        return {
            onClick: () => handleTaskSelected(task.id)
        };
    }, [handleTaskSelected]);

    const onSingleColumnSortChange = useCallback((sorter: SorterResult<UserTask>) => {
        if (!sorter.order) {
            deleteSortSearchParams(searchParams);
            setSearchParams(searchParams);
            handleSortChange();
        } else {
            const sortDirectionChanged = sorter.order && sorter.order !== taskTableSortData?.order;
            const sortFieldChanged = sorter.field && sorter.field !== taskTableSortData?.property;
            if (sortDirectionChanged || sortFieldChanged) {
                const order = sorter.order === "descend" ? "desc" : "asc";
                const property = sorter.field as string;
                setSortParams(searchParams, property, order);
                setSearchParams(searchParams);
                handleSortChange({order, property});
            }
        }
    }, [searchParams, setSearchParams, handleSortChange, taskTableSortData?.order, taskTableSortData?.property]);


    const onPaginationChange = useCallback((pagination: TablePaginationConfig) => {
        setPageDataParams(searchParams, pagination.current, pagination.pageSize);
        setSearchParams(searchParams);
        handlePageDataChange({page: pagination.current || 1, size: pagination.pageSize || 10});
    }, [searchParams, setSearchParams, handlePageDataChange]);

    const onTaskListTableChange = useCallback((pagination: TablePaginationConfig, _filters: Record<string, unknown>,
                                               sorter: SorterResult<UserTask> | SorterResult<UserTask>[], _extra: TableCurrentDataSource<unknown>) => {
        if (pagination && pagination.current !== page || pagination.pageSize !== size) {
            onPaginationChange(pagination);
        }
        if (sorter && !Array.isArray(sorter)) {
            onSingleColumnSortChange(sorter);
        }
    }, [onPaginationChange, onSingleColumnSortChange]);

    const tableScroll = totalElements && totalElements > 5 ? "20em" : undefined; //TODO: make responsive

    return (
        <>
            <Table columns={columns} rowKey="id"
                   scroll={{y: tableScroll}}
                   loading={loading}
                   rowClassName={record => record.id === selectedTaskId ? "ant-table-row-selected" : ""}
                   dataSource={data}
                   pagination={{
                       position: ["topRight", "none"],
                       total: totalElements && totalElements > 0 ? totalElements : 1, //if total is 0, pagination is hidden
                       showTotal: (total, range) =>
                           totalElements && totalElements > 0 ? `\${range[0]}-\${range[1]} of \${total} items` : undefined,
                       defaultCurrent: page,
                       current: page,
                       defaultPageSize: size,
                       pageSize: size,
                       showSizeChanger: true,
                   }}
                   onRow={onTaskRowClick} rowHoverable={true} showSorterTooltip={{target: "sorter-icon"}}
                   onChange={onTaskListTableChange}/>
        </>
    );
};