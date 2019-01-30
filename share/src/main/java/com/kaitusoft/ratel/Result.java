package com.kaitusoft.ratel;

import io.vertx.core.json.Json;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/15
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class Result implements Serializable, Cloneable {

    private static final long serialVersionUID = -60707068516687196L;

    /**
     * 响应码
     */
    private int code;

    /**
     * * = follow upstream
     */
    private String contentType = "*";

    private boolean json;

    private Object content;

//    private Map<String, String> removeHeaders;
//
//    private Map<String, String> appendHeaders;


    public Result(int code, Object content) {
        this.code = code;
        this.content = content;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        json = contentType.toLowerCase().indexOf("json") > 0;
    }

    public Result clone() {
        Result newResult = new Result();
        newResult.setCode(this.code);
        newResult.setContentType(this.contentType);

        newResult.setContent(this.content);

        return newResult;
    }

    public String toString() {
        if (json)
            return Json.encode(content);
        return content.toString();
    }

}
