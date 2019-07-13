package com.allen.db.proxy;

import com.allen.common.Constants;
import com.allen.common.entity.BusMsgType;
import com.allen.common.entity.Command;
import com.allen.common.entity.DataVO;
import com.allen.common.entity.Node;
import com.allen.db.consensus.ConsensusConfig;
import com.allen.protocol.entity.MessageType;
import com.allen.protocol.entity.NettyMessage;
import com.allen.protocol.utils.SerializationUtils;

import java.util.HashMap;
import java.util.Map;

public class AllenDBProxy {
    private volatile long leaderId;

    private final Map<Long, Node> nodes;

    public AllenDBProxy(String cluster) {
        nodes = new HashMap<>();
        String[] strNodes = ConsensusConfig.cluster.split(",");
        for (int i = 0; i < strNodes.length; i++) {
            String[] idAddress = strNodes[i].split("@");
            String[] hostPort = idAddress[1].split(":");
            Node node = new Node(Long.valueOf(idAddress[0]), hostPort[0], Integer.valueOf(hostPort[1]));
            node.getClient().registerHandler("response-handler", new ResponseHandler(node.getClient()));
            nodes.put(node.getId(), node);
        }
    }

    public Node conn(long nodeId) throws Exception {
        Node node = nodes.get(nodeId);
        if (node != null && !node.getClient().isConnected()) {
            node.getClient().connect();
        }
        return node;
    }

    public DataVO get(String key) throws Exception {
        Node node = conn(leaderId);
        return null;//todo
    }

    public DataVO set(Command command) throws Exception {
        Node leader = conn(leaderId);

        HashMap<String, String> att = new HashMap<>();
        att.put(Constants.NETTY_MESSAGE_HEADER_ATTR_BODY_TYPE, BusMsgType.REQ_CLIENT.ordinal() + "");
        NettyMessage res = leader.getClient().request(att, command);
        NettyMessage.Header header = res.getHeader();
        if (header.getType() == MessageType.REDIRECT.code()) {
            leaderId = Long.valueOf(header.getAttribute(Constants.NETTY_MESSAGE_HEADER_ATTR_REDIRECT_PATH));
            return set(command);
        }
        Command c = SerializationUtils.read((byte[]) res.getBody(), Command.class);
        return c.getRecord().get(0);
    }
}
