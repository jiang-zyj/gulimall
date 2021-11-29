package com.zyj.common.constant;

/**
 * @program: gulimall
 * @ClassName ProductConstant
 * @author: YaJun
 * @Date: 2021 - 11 - 15 - 21:32
 * @Package: com.zyj.common.constant
 * @Description:
 */
public class ProductConstant {

    public enum AttrEnum{
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private final int code;
        private final String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum{
        NEW_SPU(0, "新建"), SPU_UP(1, "上架"), SPU_DOWN(2, "下架");
        private final int code;
        private final String msg;

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }



}
