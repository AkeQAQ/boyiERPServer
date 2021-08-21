package com.boyi.common.constant;

public class DBConstant {
     public static class TABLE_ROLE_MENU{
        public static final String MENU_FIELDNAME = "menu_id";
    }

    public static class TABLE_MENU{
        public static final String PARENT_ID_FIELDNAME = "parent_id";
        public static final String STATUS_FIELDNAME = "status";
        /**
         *  0:代表 正常状态
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 禁止状态
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_USER{
        public static final String USER_ID_FIELDNAME = "user_id";

        public static final String STATUS_FIELDNAME = "status";

        /**
         *  0:代表 正常状态
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 禁止状态
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_ROLE{
        public static final String ROLE_ID_FIELDNAME = "role_id";

        public static final String STATUS_FIELDNAME = "status";

        /**
         *  0:代表 正常状态
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 禁止状态
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;

    }

    public static class TABLE_BASE_UNIT{
        public static final String STATUS_FIELDNAME = "status";
        /**
         *  0:代表 正常状态
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 禁止状态
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_BASE_MATERIAL_GROUP{
        public static final String STATUS_FIELDNAME = "status";
        /**
         *  0:代表 正常状态
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 禁止状态
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;

        public static final String PARENT_ID_FIELDNAME ="parent_id";

    }

    public static class TABLE_BASE_MATERIAL{
        public static final String NAME_FIELDNAME = "name";

        public static final String STATUS_FIELDNAME = "status";
        /**
         *  0:代表 正常状态
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 禁止状态
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

}
