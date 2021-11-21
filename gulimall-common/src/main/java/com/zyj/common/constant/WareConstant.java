package com.zyj.common.constant;

/**
 * @program: gulimall
 * @ClassName WareConstant
 * @author: YaJun
 * @Date: 2021 - 11 - 21 - 15:17
 * @Package: com.zyj.common.constant
 * @Description:
 */
public class WareConstant {

    public enum PurchaseStatusEnum{
        CREATED(0, "新建"), ASSIGNED(1, "已分配"),
        RECEIVED(2, "已领取"), FINISHED(3, "已完成"),
        HASERROR(4, "有异常");

        private final int code;
        private final String msg;

        PurchaseStatusEnum(int code, String msg) {
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


    public enum PurchaseDetailStatusEnum{
        CREATED(0, "新建"), ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"), FINISHED(3, "已完成"),
        HASERROR(4, "采购失败");

        private final int code;
        private final String msg;

        PurchaseDetailStatusEnum(int code, String msg) {
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
