package com.kaitusoft.ratel.codex;

import io.vertx.core.buffer.Buffer;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/28
 *          <p>
 *          write description here
 */
public abstract class DataDecoder {

    public abstract byte[] decode(byte[] src);

    public byte[] decode(Buffer buffer) {
        return decode(buffer.getBytes());
    }

}
