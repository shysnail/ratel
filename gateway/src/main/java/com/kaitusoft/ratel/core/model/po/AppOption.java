package com.kaitusoft.ratel.core.model.po;

import com.kaitusoft.ratel.core.common.ProtocolEnum;
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
public class AppOption implements Serializable {

    private static final long serialVersionUID = 1660411817246689946L;

    private Integer id;

    private String name;

    private ProtocolEnum protocol = ProtocolEnum.HTTP;

    private String vhost;

    private int port;

    private Timestamp createTime = new Timestamp(System.currentTimeMillis());

    private Short deployGroup = 0;

    private String description;

    private String parameter;

    private Short running;

//    public static AppOption fromJson(JsonObject jsonObject){
//        return jsonObject.mapTo(AppOption.class);
//    }

}
