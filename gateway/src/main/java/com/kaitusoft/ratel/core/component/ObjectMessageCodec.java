package com.kaitusoft.ratel.core.component;

import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.util.SerializeUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/7
 *          <p>
 *          write description here
 */
public class ObjectMessageCodec<S, R> implements MessageCodec {

    @Override
    public void encodeToWire(Buffer buffer, Object o) {
        byte[] bytes = SerializeUtil.serialize(o);
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;
        byte[] bytes = buffer.getBytes(pos, pos + length);
        return SerializeUtil.unserialize(bytes);
    }

    @Override
    public String name() {
        return Configuration.MODEL_CODEC;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    @Override
    public Object transform(Object o) {
        return o;
    }
}
