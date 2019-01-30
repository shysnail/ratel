package com.kaitusoft.ratel.cache.redis;

import java.nio.charset.Charset;

/**
 * Created by frog.w on 2017/5/5.
 */
public class StringSerializer {

    private final Charset charset;

    public StringSerializer() {
        this(Charset.forName("UTF8"));
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    public String deserialize(byte[] bytes) {
        return bytes == null?null:new String(bytes, this.charset);
    }

    public byte[] serialize(String string) {
        return string == null?null:string.getBytes(this.charset);
    }

}
