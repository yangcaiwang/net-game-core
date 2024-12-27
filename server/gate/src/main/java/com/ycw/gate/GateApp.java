package com.ycw.gate;

import com.ycw.core.cluster.enums.ServerType;
import com.ycw.core.cluster.node.ServerNode;
import com.ycw.core.cluster.node.ServerSuperNode;

public class GateApp {

    public static void main(String[] args) {
        ServerSuperNode serverSuperNode = ServerNode.valueOf(ServerType.GATE_SERVER);
        serverSuperNode.init();
        serverSuperNode.start();
        // 停服钩子
        Runtime.getRuntime().addShutdownHook(new Thread(serverSuperNode::stop));
    }
}
