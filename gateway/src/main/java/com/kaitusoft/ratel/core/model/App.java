package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.core.common.ProtocolEnum;
import com.kaitusoft.ratel.core.model.option.AppExtendOption;
import com.kaitusoft.ratel.core.model.option.SessionOption;
import com.kaitusoft.ratel.core.model.option.UpstreamOption;
import com.kaitusoft.ratel.core.model.po.AppOption;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/21
 *          <p>
 *          write description here
 */
@Data
@ToString(exclude={"apis"})
@NoArgsConstructor
public class App {
    public static final int RUNNING = 1;

    public static final int STOPPED = 0;

    public static final int PAUSED = 2;

    private Integer id;

    private String name;

    private String[] vhost;

    private int port;

    private short running;

    private ProtocolEnum protocol;

    private Preference preference;

    private CrossDomain crossDomain;

    private Ssl ssl;

    private String root;

    private SessionOption sessionOption;

    private HttpClient httpClient;

    private UpstreamOption proxyOption;

    private Blow blowSetting;

    private Map<Integer, Api> apis = new HashMap<>(16, 1.0f);

    private Map<Integer, Result> customResult = null; //new ConcurrentHashMap<>(8, 1.0f, 4);


    public App(AppOption option) throws Exception {
        this.name = option.getName();
        this.id = option.getId();
        if (!StringUtils.isEmpty(option.getVhost()) && !"*".equals(option.getVhost().trim())) {
            vhost = option.getVhost().split(" ");
        }

        this.port = option.getPort();

        this.protocol = option.getProtocol();

        this.running = option.getRunning();

        String parameter = option.getParameter();
        JsonObject extendJson = new JsonObject(parameter);
        AppExtendOption extendOption = extendJson.mapTo(AppExtendOption.class);

        if (extendOption.getPreferenceOption() != null) {
            preference = new Preference(extendOption.getPreferenceOption());
            if (extendOption.getPreferenceOption().isStaticServer())
                root = extendOption.getPreferenceOption().getRoot();
        }

        if (extendOption.getCrossDomain() != null) {
            crossDomain = extendOption.getCrossDomain();
        }

        if (protocol == ProtocolEnum.HTTP_HTTPS && extendOption.getSsl() != null) {
            ssl = extendOption.getSsl();
        }


        if (extendOption.getSessionOption() != null) {
            sessionOption = extendOption.getSessionOption();
        }

        proxyOption = extendOption.getUpstreamOption();

        if (preference.getCustomCodes() != null && preference.getCustomCodes().length > 0) {
            customResult = new ConcurrentHashMap<>(8, 1.0f, 4);
            for (Result result : preference.getCustomCodes()) {
                customResult.put(result.getCode(), result);
            }
        }

        if(!StringUtils.isEmpty(extendOption.getBlowSetting())){
            try {
                blowSetting = new JsonObject(extendOption.getBlowSetting()).mapTo(Blow.class);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(blowSetting == null)
            blowSetting = new Blow();


    }

    public void addDeployApi(Api api){
        apis.put(api.getId(), api);
    }

    public void unDeployApi(Integer id){
        apis.remove(id);
    }

    public Api getDeployApi(Integer id){
        return apis.get(id);
    }


    public boolean equals(Object another) {
        if (another instanceof App)
            return false;

        App that = (App) another;
        return (this.id != null && this.id.equals(that.getId())) || this.getPort() == that.getPort();
    }


}
