package com.kaitusoft.ratel.core.model.po;

import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * @author frog.w
 * @version 1.0.0, 2018/8/13
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class ApiOption implements Serializable {

    private static final long serialVersionUID = -8452771803828927548L;

    private Integer id;

    private Integer appId;

    private String name;

    private String vhost;

    private String path;

    private Timestamp createTime;

    private String parameter;

    private Short running;

    public static ApiOption fromJson(JsonObject jsonObject) {
//        JsonObject preferenceJson = jsonObject.getJsonObject("preferenceOption");
//        jsonObject.remove("preferenceOption");
//
//        JsonObject proxyOptionJson = jsonObject.getJsonObject("proxyOption");
//        jsonObject.remove("proxyOption");
//
//        ApiVo apiVo = jsonObject.mapTo(ApiVo.class);
//
//        if(preferenceJson != null){
//            PreferenceOption preferenceOption = preferenceJson.mapTo(PreferenceOption.class);
//            apiVo.setPreferenceOption(preferenceOption);
//        }
//
//        if(proxyOptionJson != null){
//            ProxyOption proxyOption = ProxyOption.fromJson(proxyOptionJson);
//            apiVo.setProxyOption(proxyOption);
//        }
//
//        return apiVo;

        return jsonObject.mapTo(ApiOption.class);

    }


}
