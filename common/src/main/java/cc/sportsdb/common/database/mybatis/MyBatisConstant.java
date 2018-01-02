package cc.sportsdb.common.database.mybatis;

interface MyBatisConstant {
    String METHOD_QUERY = "query";
    String METHOD_PREPARE = "prepare";

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
