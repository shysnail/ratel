package com.kaitusoft.ratel.console.model;

import java.io.Serializable;

/**
 * @author Administrator
 * @date 2017/12/27
 */
public class ExecuteResult implements Serializable {

    private static final long serialVersionUID = 4970347654699688288L;

    /**
     * 返回的状态码
     */
    private boolean success = true;


    /**
     * 信息主体，可以是各种类型的对象
     */
    private Object data;

    public ExecuteResult() {
    }

    public ExecuteResult(Object data) {
        this.success = true;
        this.data = data;
    }

    public ExecuteResult(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public ExecuteResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public Object getData() {
        return data;
    }

    public ExecuteResult setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public ExecuteResult clone() {
        ExecuteResult newObject = null;
        try {
            newObject = (ExecuteResult) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            newObject = new ExecuteResult();
            newObject.success = this.success;
            newObject.data = this.data;
        }

        return newObject;
    }
}
