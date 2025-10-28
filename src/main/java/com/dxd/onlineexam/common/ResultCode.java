package com.dxd.onlineexam.common;

/**
 * 状态码枚举
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误"),
    
    // 业务错误码
    LOGIN_ERROR(1001, "用户名或密码错误"),
    EXAM_NOT_STARTED(1002, "考试未开始"),
    EXAM_ENDED(1003, "考试已结束"),
    PAPER_SUBMITTED(1004, "试卷已提交"),
    QUESTION_NOT_FOUND(1005, "题目不存在"),
    EXAM_NOT_FOUND(1006, "考试不存在"),
    NO_PERMISSION(1007, "无权限操作该资源");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

