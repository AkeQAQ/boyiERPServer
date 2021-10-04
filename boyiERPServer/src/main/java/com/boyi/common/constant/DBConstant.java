package com.boyi.common.constant;

import java.util.HashMap;
import java.util.Map;

public class DBConstant {
    public static void main(String[] args) {
        char n = 'n';
        System.out.println((int)n);
        System.out.println((int)'s');
        System.out.println((int)'h');

        System.out.println((int)'z');
        System.out.println((int)'y');
        System.out.println((int)'q');

        System.out.println((int)'z');
        System.out.println((int)'x');
        System.out.println((int)'y');

        System.out.println((int)'w');
        System.out.println((int)'l');


    }
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
        public static final String UPDATED_USER_FIELDNAME = "update_user";

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
        public static final String UPDATED_USER_FIELDNAME = "update_user";

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
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"审核通过");
            statusMap2.put(1,"待审核");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String SUPPLIER_DOCUMENT_NUM_FIELDNAME = "supplier_document_num";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String BUY_IN_DATE_FIELDNAME = "buy_in_date";
        public static final String PRICE_DATE_FIELDNAME = "price_date";
        public static final String ORDER_ID_FIELDNAME = "order_id";

        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

        public static final String SOURCE_TYPE_FIELDNAME = "source_type";

        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 待审核
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;

        /**
         *  0:代表 采购入库
         */
        public static final Integer SOURCE_TYPE_FIELDVALUE_0 = 0;

        /**
         *  1:代表 订单入库
         */
        public static final Integer SOURCE_TYPE_FIELDVALUE_1 = 1;
    }

    public static class TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL{
        public static final String ID_FIELDNAME = "id";
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String NUM_FIELDNAME = "num";
        public static final String COMMENT_FIELDNAME = "comment";
        public static final String ORDER_SEQ_FIELDNAME = "order_seq";
        public static final String ORDER_ID_FIELDNAME = "order_id";
        public static final String ORDER_DETAIL_ID_FIELDNAME = "order_detail_id";

        public static final String PRICE_DATE_FIELDNAME = "price_date";

    }


    public static class TABLE_ORDER_BUYORDER_DOCUMENT{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"已完成");
            statusMap2.put(1,"未完成");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;


        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";

        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String ORDER_DATE_FIELDNAME = "order_date";



        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

        /**
         *  0:代表 已完成
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 未完成
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL{
        public static final String ID_FIELDNAME = "id";

        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"已下推");
            statusMap2.put(1,"未下推");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;


        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String NUM_FIELDNAME = "num";
        public static final String COMMENT_FIELDNAME = "comment";
        public static final String DONE_DATE_FIELDNAME = "done_date";
        public static final String ORDER_SEQ_FIELDNAME = "order_seq";
        public static final String STATUS_FIELDNAME = "status";

        /**
         *  0:代表 已下推
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 未下推
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;


    }

    public static class TABLE_BASE_SUPPLIER{
        public static final String NAME_FIELDNAME = "name";
        public static final String GROUP_CODE_FIELDNAME = "group_code";
        public static final String SUB_ID_FIELDNAME = "sub_id";
        public static final String GROUP_NAME_FIELDNAME = "group_name";
        public static final String ADDRESS_FIELDNAME = "address";
        public static final String MOBILE_FIELDNAME = "mobile";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "update_user";
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


    public static class TABLE_REPOSITORY_PICK_MATERIAL{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"审核通过");
            statusMap2.put(1,"待审核");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String DEPARTMENT_ID_FIELDNAME = "department_id";
        public static final String PICK_DATE_FIELDNAME = "pick_date";
        public static final String PICK_USER_FIELDNAME = "pick_user";

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

    public static class TABLE_REPOSITORY_PICK_MATERIAL_DETAIL{
        public static final String ID_FIELDNAME = "id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String NUM_FIELDNAME = "num";

    }


    public static class TABLE_REPOSITORY_RETURN_MATERIAL{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"审核通过");
            statusMap2.put(1,"待审核");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String DEPARTMENT_ID_FIELDNAME = "department_id";
        public static final String RETURN_DATE_FIELDNAME = "return_date";
        public static final String RETURN_USER_FIELDNAME = "return_user";

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

    public static class TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL{
        public static final String ID_FIELDNAME = "id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String NUM_FIELDNAME = "num";

    }


    public static class TABLE_REPOSITORY_BUYOUT_DOCUMENT{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"审核通过");
            statusMap2.put(1,"待审核");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String BUY_OUT_DATE_FIELDNAME = "buy_out_date";

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

        /**
         *  0:代表 采购入库
         */
        public static final Integer SOURCE_TYPE_FIELDVALUE_0 = 0;

        /**
         *  1:代表 订单入库
         */
        public static final Integer SOURCE_TYPE_FIELDVALUE_1 = 1;
    }

    public static class TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL{
        public static final String ID_FIELDNAME = "id";
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String NUM_FIELDNAME = "num";
        public static final String COMMENT_FIELDNAME = "comment";
        public static final String PRICE_DATE_FIELDNAME = "price_date";
    }


    public static class TABLE_ORDER_PRODUCTPRICEPRE{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"已确认");
            statusMap2.put(1,"未确认");

            statusMap2.put(2,"报价确认");
            statusMap2.put(3,"实际价确认");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "update_user";
        public static final String COMPANY_NUM_FIELDNAME = "company_num";
        public static final String COSTOMER_FIELDNAME = "customer";
        public static final String SAVE_PATH_FIELDNAME = "save_path";
        public static final String PRICE_FIELDNAME = "price";
        public static final String UPLOAD_NAME_FIELDNAME = "upload_name";

        public static final String EXCEL_JSON_FIELDNAME = "excel_json";
        public static final String REAL_JSON_FIELDNAME = "real_json";
        public static final String REAL_PRICE_FIELDNAME = "real_price";
        public static final String PRICE_LAST_UPDATE_DATE_FIELDNAME = "price_last_update_date";
        public static final String PRICE_LAST_UPDATE_USER_FIELDNAME = "price_last_update_user";
        public static final String REAL_PRICE_LAST_UPDATE_DATE_FIELDNAME = "real_price_last_update_date";
        public static final String REAL_PRICE_LAST_UPDATE_USER_FIELDNAME = "real_price_last_update_user";



        /**
         *  0:代表 已确认
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 未确认
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 报价确认
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
    }


    public static class TABLE_PRODUCE_CRAFT{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"已确认");
            statusMap2.put(1,"未确认");

            statusMap2.put(2,"开发填写确认");
            statusMap2.put(3,"最终确认");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "update_user";
        public static final String COMPANY_NUM_FIELDNAME = "company_num";
        public static final String COSTOMER_FIELDNAME = "customer";
        public static final String EXCEL_JSON_NAME_FIELDNAME = "excel_json";
        public static final String REAL_JSON_FIELDNAME = "real_json";


        /**
         *  0:代表 已确认
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 未确认
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 报价确认
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
    }

    public static class TABLE_SPREAD_DEMO{

        public static final String ID_FIELDNAME = "id";
        public static final String TYPE_FIELDNAME = "type";
        public static final String DEMO_JSON_FIELDNAME = "demo_json";



        /**
         *  报价模板
         */
        public static final Integer TYPE_BAOJIA_FIELDVALUE_0 = 0;

        /**
         *  工艺单模板
         */
        public static final Integer TYPE_GYD_FIELDVALUE_1 = 1;
    }


    public static class TABLE_REPOSITORY_CHECK{
        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"审核通过");
            statusMap2.put(1,"待审核");
            statusMap = statusMap2;
        }
        public static final Map<Integer,String> statusMap;

        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";
        public static final String CHECK_DATE_FIELDNAME = "check_date";
        public static final String CHECK_USER_FIELDNAME = "check_user";

        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String PRODUCE_DOC_NUM_FIELDNAME = "produce_doc_num";

        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 待审核
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
    }

    public static class TABLE_REPOSITORY_CHECK_DETAIL{
        public static final String ID_FIELDNAME = "id";
        public static final String DOCUMENT_ID_FIELDNAME = "document_id";
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String CHECK_NUM_FIELDNAME = "check_num";
        public static final String CHANGE_NUM_FIELDNAME = "change_num";

    }


    public static class TABLE_REPOSITORY_CLOSE{
        public static final String ID_FIELDNAME = "id";
        public static final String CLOSE_DATE_FIELDNAME = "close_date";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

    }


}
