export const parseSortOrder = (sort?: string) => {
    if (!sort) {
        return undefined;
    }
    return sort === "asc" ? "ascend" : "descend";
}