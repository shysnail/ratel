package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.core.handler.DefaultAccessLimit;
import com.kaitusoft.ratel.core.model.option.AccessLimitOption;
import com.kaitusoft.ratel.core.model.option.AuthOption;
import com.kaitusoft.ratel.core.model.option.EdgeProcessorOption;
import com.kaitusoft.ratel.core.model.option.PreferenceOption;
import com.kaitusoft.ratel.handler.AbstractAuthProcessor;
import com.kaitusoft.ratel.handler.ExtendableProcessor;
import com.kaitusoft.ratel.handler.Processor;
import com.kaitusoft.ratel.util.ResourceUtil;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.http.HttpMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/21
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class Preference {

    /**
     * 请求类型
     * null表示所有
     */
    private HttpMethod[] method;

    private String[] ipBlacklist;

    /**
     * null 不开启
     * "" 使用app配置
     * 非空
     */
    private String docRoot;

    private Processor accessLimit;

    private Processor xssFilter;

    private Processor sqlFilter;

    private AbstractAuthProcessor auth;

    private ExtendableProcessor[] preProcessors;

    private ExtendableProcessor[] postProcessors;

    private Result[] customCodes;

    public Preference(PreferenceOption option) throws Exception {
        if (!StringUtils.isEmpty(option.getMethod())) {
            String[] methods = option.getMethod().split(",");
            method = new HttpMethod[methods.length];
            for (int i = 0; i < methods.length; i++) {
                method[i] = HttpMethod.valueOf(methods[i]);
            }
        }

        if (option.getIpBlacklist() != null) {
            ipBlacklist = new String[option.getIpBlacklist().length];
            System.arraycopy(option.getIpBlacklist(), 0, ipBlacklist, 0, ipBlacklist.length);
        }

        if (option.getRoot() != null)
            this.docRoot = option.getRoot();

        //创建 访问限制
        AccessLimitOption accessLimitOption = option.getAccessLimitOption();
        if (accessLimitOption != null) {
            accessLimit = new DefaultAccessLimit(accessLimitOption);
        }

        //创建权限认证
        AuthOption authOption = option.getAuthOption();
        if (option.getAuthOption() != null) {
            auth = ResourceUtil.instanceClass(authOption.getInstance(), AbstractAuthProcessor.class);
            auth.setResult(authOption.getFailReturn().clone());
        }

        //前置处理器
        List<EdgeProcessorOption> preProcessorOptions = option.getPreProcessors();
        if (preProcessorOptions != null && preProcessorOptions.size() > 0) {
            preProcessors = new ExtendableProcessor[preProcessorOptions.size()];

            for (int i = 0; i < preProcessorOptions.size(); i++) {
                preProcessors[i] = ResourceUtil.instanceClass(preProcessorOptions.get(i).getInstance(), ExtendableProcessor.class);
            }

        }

        //后置处理器

        List<EdgeProcessorOption> postProcessorOptions = option.getPostProcessors();
        if (postProcessorOptions != null && postProcessorOptions.size() > 0) {
            postProcessors = new ExtendableProcessor[postProcessorOptions.size()];

            for (int i = 0; i < postProcessorOptions.size(); i++) {
                postProcessors[i] = ResourceUtil.instanceClass(postProcessorOptions.get(i).getInstance(), ExtendableProcessor.class);
            }

        }

        List<Result> customCodes = option.getCustomCodes();
        if (customCodes != null && customCodes.size() > 0) {
            this.customCodes = new Result[customCodes.size()];
            for (int i = 0; i < customCodes.size(); i++) {
                this.customCodes[i] = customCodes.get(i).clone();
            }
        }
    }


}
