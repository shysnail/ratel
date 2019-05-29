package com.kaitusoft.ratel.core.common;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.util.StringUtils;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/19
 *          <p>
 *          write description here
 */
public class Event {

    public static final String INIT_ENV = "r:sys:init";

    public static final String SYSTEM_INFO_GET = "r:sys:info.get";

    public static final String USER_GET = "r:u:get";

    public static final String USER_FIND = "r:u:find";

    public static final String USER_ADD = "r:u:add";

    public static final String USER_UPDATE = "r:u:update";

    public static final String USER_UPDATE_PASSWORD = "r:u:update.ps";

    public static final String USER_FROZEN = "r:u:update.frozen";

    public static final String FIND_ALL_APP = "r:d:a.list";

    public static final String FIND_APP_COND = "r:d:a:cond";

    public static final String GET_APP = "r:d:a.get";

    public static final String ADD_APP = "r:d:a.add";

    public static final String UPDATE_APP = "r:d:a.update";

    public static final String UPDATE_APP_PROP = "r:d:a.update.prop";

    public static final String DELETE_APP = "r:d:a.delete";

    public static final String FIND_ALL_GROUP = "r:d:g.list";

    public static final String FIND_GROUP_IDS = "r:d:g.find.id";

    public static final String GET_GROUP = "r:d:g.get";

    public static final String ADD_GROUP = "r:d:g.add";

    public static final String UPDATE_GROUP = "r:d:g.update";

    public static final String DELETE_GROUP = "r:d:g.delete";

    public static final String FIND_API = "r:d:api.list";

    public static final String FIND_API_COND = "r:d:api.find";

    public static final String GET_API = "r:d:api.get";

    public static final String ADD_API = "r:d:api.add";

    public static final String UPDATE_API = "r:d:api.update";

    public static final String UPDATE_API_PROP = "r:d:api.update.prop";

    public static final String DELETE_API = "r:d:api.delete";

    public static final String GET_APP_API = "r:d:a.api.get";

    public static final String RUN_ON_START = "r:d:a.run.auto";

    public static final String START_APP = "r:g:a.start";

    public static final String RESTART_APP = "r:g:a.restart";

    public static final String STOP_APP = "r:g:a.stop";

    public static final String START_API = "r:g:api.start";

    public static final String RESTART_API = "r:g:api.restart";

    public static final String STOP_API = "r:g:api.stop";

    public static final String PAUSE_API = "r:g:api.pause";
    public static final String RESUME_API = "r:g:api.rerun";


    public static final String START_APP_ACT = "r:g:a.start.act";

    public static final String RESTART_APP_ACT = "r:g:a.restart.act";

    public static final String STOP_APP_ACT = "r:g:a.stop.act";

    public static final String START_API_ACT = "r:g:api.start.act";

    public static final String RESTART_API_ACT = "r:g:api.restart.act";

    public static final String STOP_API_ACT = "r:g:api.stop.act";

    public static final String PAUSE_API_ACT = "r:g:api.pause.act";
    public static final String RESUME_API_ACT = "r:g:api.rerun.act";

    public static final String APP_STATUS = "r:g:a.status";
    public static final String API_STATUS = "r:g:api.status";

    public static final String ACTION_APP_DEPLOYED = "r:a:a.deploy";
    public static final String ACTION_APP_UNDEPLOYED = "r:a:a.undeploy";

    public static final String ACTION_REQUEST = "r:a:req";
    public static final String ACTION_REQUEST_DONE = "r:a:req.done";
    public static final String ACTION_REQUEST_FAIL = "r:a:req.fail";
    public static final String ACTION_REQUEST_ERROR = "r:a:req.error";

    public static final String APP_STATUS_ADD = "r:s:a.add";
    public static final String APP_STATUS_FIND = "r:s:a.find";
    public static final String APP_STATUS_DELETE = "r:s:a.delete";

    public static final String STATUS_ADD = "r:s:add";
    public static final String STATUS_FIND = "r:s:find";
    public static final String STATUS_DELETE = "r:s:delete";

    public static final String CLUSTER_NODE_APP_STATUS = "r:c:a.status";

    public static final String CLUSTER_NODE_API_STATUS = "r:c:api.status";

    public static final String CLUSTER_NODE_ADD = "r:c:n.add";
    public static final String CLUSTER_NODE_OFFLINE = "r:c:n.left";
    public static final String CLUSTER_EXPEL_NODE = "r:c:n.remove";

    public static final String CLUSTER_GET_NODE = "r:c:node";

    public static final String CLUSTER_GET_NODES = "r:c:nodes";
    public static final String CLUSTER_HALT_NODE = "r:c:n.halt";
    public static final String CLUSTER_RESTART_NODE = "r:c:n.restart";


    public static final String CLUSTER_GROUPING = "r:c:n.group";
    public static final String CLUSTER_CALLBACK = "r:c:callback";


//    public static final String CLUSTER_START_APP_ALL = "r:c:a.start.all";
//    public static final String CLUSTER_START_APP = "r:c:a.start";
//    public static final String CLUSTER_STOP_APP = "r:c:a.stop";
//    public static final String CLUSTER_START_API = "r:c:api.start";
//    public static final String CLUSTER_STOP_API = "r:c:api.stop";


    public static String formatAddress(String event, Object... sign) {
        StringBuilder sb = new StringBuilder();
        sb.append(event);
        if (sign != null && sign.length > 0) {
            sb.append("-");
            for (int i = 0; i < sign.length; i++) {
                sb.append(sign[i]).append(".");
            }
        }

//        debug模式
        if (!StringUtils.isEmpty(System.getProperty("debug"))) {

        }

        return sb.toString();
    }

    /**
     * 内部地址，强制加入节点标志
     *
     * @param event
     * @param sign
     * @return
     */
    public static String formatInternalAddress(String event, Object... sign) {
        int signLength = 1;
        if (sign != null && sign.length > 0) {
            signLength = sign.length + 1;
        }
        Object[] newSign = new Object[signLength];

        newSign[0] = ClusterVerticle.myNodeId;
        if (signLength > 1) {
            System.arraycopy(sign, 0, newSign, 1, sign.length);
        }
        return formatAddress(event, newSign);
    }

}
