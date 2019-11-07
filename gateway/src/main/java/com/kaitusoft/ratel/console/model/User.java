package com.kaitusoft.ratel.console.model;

import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/21
 *          <p>
 *          write description here
 */
public class User extends AbstractUser {

    private Set<String> myResources;

    private JsonObject principal;

    @Override
    protected void doIsPermitted(String s, Handler<AsyncResult<Boolean>> handler) {
        boolean pass = false;
        for (String path : myResources) {
            if (StringUtils.isMatch(s, path)) {
                pass = true;
                break;
            }
        }
        handler.handle(Future.<Boolean>succeededFuture((s != null && pass)));
    }

    /**
     * 序列化自定义属性
     *
     * @param buff
     */
    @Override
    public void writeToBuffer(Buffer buff) {
        super.writeToBuffer(buff);

        JsonObject data = new JsonObject();
        if (myResources != null)
            data.put("resources", new JsonArray(new ArrayList(myResources)));
        if (principal != null)
            data.put("principal", principal);

        Buffer dataBuffer = data.toBuffer();
        buff.appendInt(dataBuffer.length()).appendBuffer(dataBuffer);
    }

    /**
     * 反序列化自定义属性
     *
     * @param pos
     * @param buffer
     * @return
     */
    @Override
    public int readFromBuffer(int pos, Buffer buffer) {
        int posLocal = super.readFromBuffer(pos, buffer);
        if (buffer.length() < posLocal + 4)
            return posLocal;

        int dataLength = buffer.getInt(posLocal);
        if (buffer.length() < posLocal + 4 + dataLength)
            return posLocal;

        posLocal += 4;
        byte[] data = buffer.getBytes(posLocal, posLocal + dataLength);
        posLocal += dataLength;

        JsonObject jsonData = new JsonObject(new String(data));
        JsonArray resources = jsonData.getJsonArray("resources");
        if (resources != null) {
            myResources = new HashSet<>();
            resources.forEach(obj -> {
                myResources.add((String) obj);
            });
        }

        principal = jsonData.getJsonObject("principal");

        return posLocal;

    }

    @Override
    public JsonObject principal() {
        return principal;
    }

    public void setPrincipal(JsonObject principal) {
        this.principal = principal;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {

    }
}
