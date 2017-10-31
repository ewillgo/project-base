package cc.sportsdb.common.database.config;

import java.util.List;

public class DataSourceList {
    private final List<String> dataSourceNames;
    private final String defaultDataSourceName;

    public DataSourceList(List<String> dataSourceNames) {
        this.dataSourceNames = dataSourceNames;
        if (this.dataSourceNames == null || this.dataSourceNames.isEmpty()) {
            throw new IllegalArgumentException("DataSource name must be supply at least one");
        }
        this.defaultDataSourceName = this.dataSourceNames.get(0);
    }

    public List<String> getDataSourceNames() {
        return dataSourceNames;
    }

    public String getDefaultDataSourceName() {
        return defaultDataSourceName;
    }
}
