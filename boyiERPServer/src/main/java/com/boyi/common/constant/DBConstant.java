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
    public static class TABLE_TAG{
        public static final String TAG_NAME_FIELDNAME = "tag_name";
        public static final String TYPE_FIELDNAME = "type";
        public static final String CREATED_FIELDNAME = "created";
        public static final String CREATED_TIME_FIELDNAME = "created_time";

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
        public static final String PRIORITY_FIELDNAME = "priority";

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
        public static final String STATUS_FIELDNAME = "status";
        public static final String LOW_WARNING_LINE_FIELDNAME = "low_warning_line";


        /**
         *  null:代表 启用
         */
        public static final Integer STATUS_FIELDVALUE_NULL = null;

        /**
         *  -1:代表 禁用
         */
        public static final Integer STATUS_FIELDVALUE_F1 = -1;


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
            statusMap2.put(0,"已审核");
            statusMap2.put(1,"暂存");
            statusMap2.put(2,"审核中");
            statusMap2.put(3,"重新审核"); // 等同于 审核中。
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
         *  1:代表 暂存
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 审核中
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 重新审核
         */
        public static final Integer STATUS_FIELDVALUE_3 = 3;

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
        public static final String DETAIL_STATUS_FIELDNAME = "detail_status";//detail表得status字段，

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
            statusMap2.put(0,"已审核");
            statusMap2.put(1,"暂存");
            statusMap2.put(2,"审核中");
            statusMap2.put(3,"重新审核"); // 等同于 审核中。
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
        public static final String COMMENT_FIELDNAME = "comment";
        public static final String BATCH_ID_FIELDNAME = "batch_id";


        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 暂存
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 审核中
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 重新审核
         */
        public static final Integer STATUS_FIELDVALUE_3 = 3;
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
            statusMap2.put(0,"已审核");
            statusMap2.put(1,"暂存");
            statusMap2.put(2,"审核中");
            statusMap2.put(3,"重新审核"); // 等同于 审核中。
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
        public static final String BATCH_ID_FIELDNAME = "batch_id";


        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 暂存
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 审核中
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 重新审核
         */
        public static final Integer STATUS_FIELDVALUE_3 = 3;
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
            statusMap2.put(0,"已审核");
            statusMap2.put(1,"暂存");
            statusMap2.put(2,"审核中");
            statusMap2.put(3,"重新审核"); // 等同于 审核中。
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
         *  1:代表 暂存
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 审核中
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 重新审核
         */
        public static final Integer STATUS_FIELDVALUE_3 = 3;

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

            Map<Integer,String> statusMap3 = new HashMap<Integer,String>();
            statusMap3.put(0,"正常");
            statusMap3.put(1,"无法计算");

            statusMap3.put(2,"盈利");
            statusMap3.put(3,"亏损");
            statusMa3 = statusMap3;
        }
        public static final Map<Integer,String> statusMap;
        public static final Map<Integer,String> statusMa3;

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
        public static final String DEAL_PRICE_FIELDNAME = "deal_price";

        public static final String UPLOAD_NAME_FIELDNAME = "upload_name";

        public static final String EXCEL_JSON_FIELDNAME = "excel_json";
        public static final String REAL_JSON_FIELDNAME = "real_json";
        public static final String REAL_PRICE_FIELDNAME = "real_price";
        public static final String PRICE_LAST_UPDATE_DATE_FIELDNAME = "price_last_update_date";
        public static final String PRICE_LAST_UPDATE_USER_FIELDNAME = "price_last_update_user";
        public static final String REAL_PRICE_LAST_UPDATE_DATE_FIELDNAME = "real_price_last_update_date";
        public static final String REAL_PRICE_LAST_UPDATE_USER_FIELDNAME = "real_price_last_update_user";
        public static final String YK_STATUS_FIELDNAME = "yk_status";



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

        /**
         *  0:代表 正常
         */
        public static final Integer YK_STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 无法计算
         */
        public static final Integer YK_STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 盈利
         */
        public static final Integer YK_STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 亏损
         */
        public static final Integer YK_STATUS_FIELDVALUE_3 = 3;

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

    public static class TABLE_REPOSITORY_INOUT_DETAIL{
        public static final String ID_FIELDNAME = "id";
        public static final String CLOSE_DATE_FIELDNAME = "close_date";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";

    }

    public static class TABLE_PRODUCE_RETURN_SHOES{
        public static final String USER_NAME_FIELDNAME = "user_name";
        public static final String PACKAGE_NO_FIELDNAME = "package_no";
        public static final String USER_ART_NO_FIELDNAME = "user_art_no";
        public static final String USER_REQUEST_FIELDNAME = "user_request";
        public static final String DEAL_SITUATION_FIELDNAME = "deal_situation";
        public static final String RETURN_DATE_FIELDNAME = "return_date";
        public static final String CREATED_FIELDNAME = "created";
    }

    public static class TABLE_BUY_MATERIAL_SUPPLIER{
        public static final String INNERT_MATERIAL_ID_FIELDNAME = "innert_material_id";
        public static final String SUPPLIER_ID_FIELDNAME = "supplier_id";
        public static final String SUPPLIER_NAME_FIELDNAME = "supplier_name";
        public static final String SUPPLIER_MATERIAL_ID_FIELDNAME = "supplier_material_id";
        public static final String SUPPLIER_MATERIAL_NAME_FIELDNAME = "supplier_material_name";
        public static final String SUPPLIER_MATERIAL_PRICE_FIELDNAME = "supplier_material_price";
        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String ID_FIELDNAME = "id";

    }

    public static class TABLE_PRODUCE_PRODUCT_CONSTITUENT{
        public static final Map<Integer,String> statusMap;

        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"已审核");
            statusMap2.put(1,"暂存");
            statusMap2.put(2,"审核中");
            statusMap2.put(3,"重新审核"); // 等同于 审核中。
            statusMap = statusMap2;
        }
        public static final String PRODUCT_NUM_FIELDNAME = "product_num";
        public static final String PRODUCT_BRAND_FIELDNAME = "product_brand";
        public static final String PRODUCT_COLOR_FIELDNAME = "product_color";
        public static final String STATUS_FIELDNAME = "status";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String ID_FIELDNAME = "id";

        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 暂存
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 审核中
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 重新审核
         */
        public static final Integer STATUS_FIELDVALUE_3 = 3;

    }

    public static class TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL{
        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String DOSAGE_FIELDNAME = "dosage";
        public static final String CONSTITUENT_ID_FIELDNAME = "constituent_id";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String ID_FIELDNAME = "id";

    }

    public static class TABLE_ORDER_PRODUCT_ORDER{
        public static final Map<Integer,String> statusMap;
        public static final Map<Integer,String> orderTypeMap;

        static{
            Map<Integer,String> statusMap2 = new HashMap<Integer,String>();
            statusMap2.put(0,"已审核");
            statusMap2.put(1,"暂存");
            statusMap2.put(2,"审核中");
            statusMap2.put(3,"重新审核"); // 等同于 审核中。
            statusMap = statusMap2;

            Map<Integer,String> orderTypeMap2 = new HashMap<Integer,String>();
            orderTypeMap2.put(0,"订单");
            orderTypeMap2.put(1,"回单");
            orderTypeMap2.put(2,"取消");
            orderTypeMap = orderTypeMap2;
        }
        public static final String ORDER_NUM_FIELDNAME = "order_num";
        public static final String CUSTOMER_NUM_FIELDNAME = "customer_num";
        public static final String PRODUCT_REGION_FIELDNAME = "product_region";
        public static final String ORDER_TYPE_FIELDNAME = "order_type";
        public static final String COMMENT_FIELDNAME = "comment";


        public static final String PRODUCT_NUM_FIELDNAME = "product_num";
        public static final String PRODUCT_BRAND_FIELDNAME = "product_brand";
        public static final String PRODUCT_COLOR_FIELDNAME = "product_color";
        public static final String STATUS_FIELDNAME = "status";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String ID_FIELDNAME = "id";

        public static final String PREPARED_FIELDNAME = "prepared";


        /**
         *  0:代表 审核通过
         */
        public static final Integer STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 暂存
         */
        public static final Integer STATUS_FIELDVALUE_1 = 1;
        /**
         *  2:代表 审核中
         */
        public static final Integer STATUS_FIELDVALUE_2 = 2;
        /**
         *  3:代表 重新审核
         */
        public static final Integer STATUS_FIELDVALUE_3 = 3;

        /**
         *  0:代表 订单
         */
        public static final Integer ORDER_TYPE_FIELDVALUE_0 = 0;

        /**
         *  1:代表 回单
         */
        public static final Integer ORDER_TYPE_FIELDVALUE_1 = 1;

        /**
         *  1:代表 订单取消
         */
        public static final Integer ORDER_TYPE_FIELDVALUE_2 = 2;

        public static final Integer PREPARED_FIELDVALUE_0 = 0;//备料完成
        public static final Integer PREPARED_FIELDVALUE_1 = 1;//备料未确认
        public static final Integer PREPARED_FIELDVALUE_2 = 2;//备料已确认


    }

    public static class TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS{
        public static final String ORDER_ID_FIELDNAME = "order_id";

        public static final String MATERIAL_ID_FIELDNAME = "material_id";
        public static final String PREPARED_NUM_FIELDNAME = "prepared_num";
        public static final String IN_NUM_FIELDNAME = "in_num";
        public static final String CAL_NUM_FIELDNAME = "cal_num";
        public static final String PROGRESS_PERCENT_NUM_FIELDNAME = "progress_percent";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String ID_FIELDNAME = "id";

        public static final String COMMENT_FIELDNAME = "comment";
        public static final String COMPLEMENT_STATUS_FIELDNAME = "complement_status";


        /**
         *  0:代表 审核通过
         */
        public static final Integer COMPLEMENT_STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 审核中
         */
        public static final Integer COMPLEMENT_STATUS_FIELDVALUE_1 = 1;

    }


    public static class TABLE_PRODUCE_BATCH{
        public static final String ORDER_NUM_FIELDNAME = "order_num";

        public static final String BATCH_ID_FIELDNAME = "batch_id";

        public static final String CREATED_USER_FIELDNAME = "created_user";
        public static final String UPDATED_USER_FIELDNAME = "updated_user";
        public static final String CREATED_FIELDNAME = "created";
        public static final String UPDATED_FIELDNAME = "updated";
        public static final String ID_FIELDNAME = "id";
        public static final String STATUS_FIELDNAME = "status";


        /**
         *  0:代表 审核通过
         */
        public static final Integer BATCH_STATUS_FIELDVALUE_0 = 0;

        /**
         *  1:代表 审核中
         */
        public static final Integer BATCH_STATUS_FIELDVALUE_1 = 1;

    }

}
