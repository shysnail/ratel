package com.kaitusoft.ratel.console.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/4
 *          <p>
 *          write description here
 */
@Data
@ToString
public class ErrorInfo implements Serializable{

    private static final long serialVersionUID = -7693979769291725320L;

    public static final int SHOW = 1;
    public static final int DONT_SHOW = 0;

    public static final int SHOW_TYPE_NORMAL = 0;
    public static final int SHOW_TYPE_HTML = 1;

    private int show = SHOW;

    private String code = "-1";

    /**
     * 信息显示类型，
     *      0= 普通toast
     *      1= content 为html脚本（js等等，包含样式元素和控制信息），一般不使用此种方式
     */
    private int showType = SHOW_TYPE_NORMAL;

    private String content;

    public ErrorInfo(String content) {
        this.content = content;
    }

    public ErrorInfo(int show, String content) {
        this.show = show;
        this.content = content;
    }

    public ErrorInfo(String code, String content) {
        this.code = code;
        this.content = content;
    }

    public ErrorInfo(String code, int show, int showType, String content) {
        this.code = code;
        this.show = show;
        this.showType = showType;
        this.content = content;
    }

}
