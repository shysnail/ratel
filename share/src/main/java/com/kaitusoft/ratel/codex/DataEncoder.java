package com.kaitusoft.ratel.codex;

import io.vertx.core.buffer.Buffer;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/28
 *          <p>
 *          write description here
 */
public abstract class DataEncoder {

    public abstract byte[] encode(byte[] src);

    public byte[] encode(Buffer buffer) {
        return encode(buffer.getBytes());
    }

}
