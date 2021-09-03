package com.boyi.common.constant;

public class DBConstant {
     public static class TABLE_ROLE_MENU{
         public static final String ID_FIELDNAME = "id";
         public static final String MENU_ID_FIELDNAME = "menu_id";
        public static final String ROLE_ID_FIELDNAME = "role_id";
     }

    public static class TABLE_MENU{
        public static final String PARENT_ID_FIELDNAME = "parent_id";
        public static final String MENU_NAME_FIELDNAME = "menu_name";
        public static final String URL_FIELDNAME = "url";
        public static final String AUTHORITY_FIELDNAME = "authority";
        public static final String COMPONENT_FIELDNAME = "component";
        public static final String TYPE_FIELDNAME = "type";
        public static final String ICON_FIELDNAME = "icon";
        public static final String ORDER_TYPE_FIELDNAME = "orderType";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";

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

    public static class TABLE_USER_ROLE{
        public static final String ID_FIELDNAME = "id";

        public static final String USER_ID_FIELDNAME = "user_id";
        public static final String ROLE_ID_FIELDNAME = "role_id";
    }

    public static class TABLE_USER{
        public static final String USER_NAME_FIELDNAME = "user_name";
        public static final String PASSWORD_FIELDNAME = "password";
        public static final String EMAIL_FIELDNAME = "email";
        public static final String CITY_FIELDNAME = "city";
        public static final String LAST_LOGIN_FIELDNAME = "last_login";
        public static final String MOBILE_FIELDNAME = "mobile";

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
        public static final String ROLE_NAME_FIELDNAME = "role_name";
        public static final String CODE_FIELDNAME = "code";
        public static final String CONTENT_FIELDNAME = "content";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
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
        public static final String NAME_FIELDNAME ="name";
        public static final String CODE_FIELDNAME ="code";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
    }

    public static class TABLE_BASE_MATERIAL_GROUP{
        public static final String PARENT_ID_FIELDNAME ="parent_id";
        public static final String NAME_FIELDNAME ="name";
        public static final String CODE_FIELDNAME ="code";
        public static final String AUTO_SUB_ID_FIELDNAME ="auto_sub_id";

        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
    }

    public static class TABLE_BASE_SUPPLIER_GROUP{
        public static final String PARENT_ID_FIELDNAME ="parent_id";
        public static final String NAME_FIELDNAME ="name";
        public static final String CODE_FIELDNAME ="code";
        public static final String AUTO_SUB_ID_FIELDNAME ="auto_sub_id";

        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";

    }

    public static class TABLE_BASE_MATERIAL{
         public static final String ID = "id";
        public static final String NAME_FIELDNAME = "name";
        public static final String GROUP_CODE_FIELDNAME = "group_code";
        public static final String UNIT_FIELDNAME = "unit";
        public static final String SPECS_FIELDNAME = "specs";
        public static final String PIC_URL_FIELDNAME = "pic_url";
        public static final String SUB_ID_FIELDNAME = "sub_id";

        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

    }
    public static class TABLE_BASE_SUPPLIER_MATERIAL{

        public static final String STATUS_FIELDNAME = "status";
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String PRICE_FIELDNAME = "price";
        public static final String START_DATE_FIELDNAME = "start_date";
        public static final String END_DATE_FIELDNAME = "end_date";
        public static final String COMMENT_FIELDNAME = "comment";

        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 待审核
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_REPOSITORY_BUYIN_DOCUMENT{
        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String SUPPLIER_DOCUMENT_NUM_FIELDNAME = "supplier_document_num";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String BUY_IN_DATE_FIELDNAME = "buy_in_date";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 待审核
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL{
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String NUM_FIELDNAME = "num";
        public static final String COMMENT_FIELDNAME = "comment";
    }

    public static class TABLE_BASE_SUPPLIER{
        public static final String NAME_FIELDNAME = "name";
        public static final String GROUP_CODE_FIELDNAME = "group_code";
        public static final String SUB_ID_FIELDNAME = "sub_id";
        public static final String GROUP_NAME_FIELDNAME = "group_name";
        public static final String ADDRESS_FIELDNAME = "address";
        public static final String MOBILE_FIELDNAME = "mobile";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
    }

    public static class TABLE_BASE_DEPARTMENT{
        public static final String NAME_FIELDNAME = "name";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
    }

    public static class TABLE_REPOSITORY_STOCK{
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String NUM_FIELDNAME = "num";

    }
}
