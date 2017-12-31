package cc.sportsdb.common.database.mybatis;

interface MyBatisConstant {
    String METHOD_QUERY = "query";
    String METHOD_PREPARE = "prepare";

    String FIELD_PAGE_NO = "pageNo";
    String FIELD_PAGE_SIZE = "pageSize";

    enum DBType {

        MYSQL("MYSQL"), ORACLE("ORACLE");

        private String name;

        DBType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static DBType of(String name) {
            DBType[] dbTypes = DBType.values();
            for (DBType dbType : dbTypes) {
                if (dbType.getName().equalsIgnoreCase(name)) {
                    return dbType;
                }
            }
            return null;
        }
    }
}
