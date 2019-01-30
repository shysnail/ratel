package com.kaitusoft.ratel.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author frog.w
 * @version 1.0.0, 2018/10/11
 *          <p>
 *          write description here
 */
@Data
@NoArgsConstructor
public class Network {
    private String name;

    private long inBytes;

    private long outBytes;

    private long inPackets;

    private long outPackets;

    private long inError;

    private long outError;

    private long inDrop;

    private long outDrop;
}
