package com.kaitusoft.ratel.core.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/18
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Node implements Serializable {

    private static final long serialVersionUID = 2256119166064410314L;

    private String hostname;

    private String nodeId;

    private String groupId;

    private Instant addTime = Instant.now();

    private boolean online;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
