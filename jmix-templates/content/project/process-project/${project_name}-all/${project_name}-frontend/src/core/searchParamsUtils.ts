const TASK_URL_PARAM_NAME = "task";
const PAGE_URL_PARAM_NAME = "page";
const PAGE_SIZE_URL_PARAM_NAME = "size";
const SORT_BY_URL_PARAM_NAME = "sortBy";
const SORT_ORDER_URL_PARAM_NAME = "order";

export const setTaskParam = (searchParams: URLSearchParams, taskId: string) => {
    searchParams.set(TASK_URL_PARAM_NAME, taskId);
}

export const getTaskParam = (searchParams: URLSearchParams) => {
    return searchParams.get(TASK_URL_PARAM_NAME);
}

export const deleteTaskParam = (searchParams: URLSearchParams) => {
    searchParams.delete(TASK_URL_PARAM_NAME);
}

export const getPageDataParams = (searchParams: URLSearchParams) => {
    const pageString = searchParams.get(PAGE_URL_PARAM_NAME);
    const page = pageString ? Number(pageString) : undefined;

    const pageSizeString = searchParams.get(PAGE_SIZE_URL_PARAM_NAME);
    const size = pageSizeString ? Number(pageSizeString) : undefined;
    if (!size || !page) {
        return undefined;
    }
    return {page, size};
}

export const setPageDataParams = (searchParams: URLSearchParams, page?: number, size?: number) => {
    searchParams.set(PAGE_URL_PARAM_NAME, String(page));
    searchParams.set(PAGE_SIZE_URL_PARAM_NAME, String(size));
}

export const setSortParams = (searchParams: URLSearchParams, sort: string, order: string) => {
    searchParams.set(SORT_BY_URL_PARAM_NAME, sort);
    searchParams.set(SORT_ORDER_URL_PARAM_NAME, order);
}

export const getSortParams = (searchParams: URLSearchParams) => {
    const property = searchParams.get(SORT_BY_URL_PARAM_NAME);
    const order = searchParams.get(SORT_ORDER_URL_PARAM_NAME);
    if (!order || !property) {
        return undefined;
    }
    return {
        order: order,
        property: property
    };
}

export const deleteSortSearchParams = (searchParams: URLSearchParams) => {
    searchParams.delete(SORT_ORDER_URL_PARAM_NAME);
    searchParams.delete(SORT_BY_URL_PARAM_NAME);
}