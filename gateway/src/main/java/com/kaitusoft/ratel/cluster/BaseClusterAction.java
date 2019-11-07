package com.kaitusoft.ratel.cluster;

import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/17
 *          <p>
 *          write description here
 */
public abstract class BaseClusterAction {

    private static final Logger logger = LoggerFactory.getLogger(BaseClusterAction.class);

    protected String myNodeId;

    protected Integer myGroup;

    protected Vertx vertx;


    public BaseClusterAction(Vertx vertx) {
        myNodeId = ClusterVerticle.myNodeId;
        myGroup = ClusterVerticle.myGroup;
        this.vertx = vertx;
    }

    /**
     * 组装公共回应消息
     *
     * @param received
     * @return
     */
    protected JsonObject buildCommonReply(JsonObject received) {
        JsonObject reply = new JsonObject();
        if (!myJob(received)) {
            reply.put("deal", false);
        } else {
            reply.put("deal", true);
        }

        reply.put("node", myNodeId);
        reply.put("nodeName", Configuration.hostname);
        reply.put("msgId", received.getString("msgId"));

        return reply;
    }

    /**
     * 不处理的情况
     * 1.指定了目标节点，但本节点不在范围内
     * 2.忽略本节点
     * 3.指定了组，但不是本节点所属组
     *
     * @param data
     * @return
     */
    protected boolean myJob(JsonObject data) {
        Integer targetGroup = data.getInteger("groupId", -1);
        String ignoreNodes = data.getString("ignoreNodes");
        String targetNodes = data.getString("targetNodes");

        //nodeId结构复杂，不担心有 ',' 导致的重复歧义
        if (!StringUtils.isEmpty(targetNodes)) {
            if (targetNodes.indexOf(myNodeId) < 0) {
                return false;
            }
        }

        if (!StringUtils.isEmpty(ignoreNodes) && ignoreNodes.indexOf(myNodeId) >= 0) {
            return false;
        }

        if (targetGroup > 0 && !targetGroup.equals(myGroup)) {
            return false;
        }

        return true;
    }
}
