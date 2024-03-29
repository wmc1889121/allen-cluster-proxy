package com.allen.db.proxy;

import com.allen.common.Constants;
import com.allen.common.entity.BusMsgType;
import com.allen.common.entity.Command;
import com.allen.common.entity.DataVO;
import com.allen.common.entity.Node;
import com.allen.protocol.client.AllenClient;
import com.allen.protocol.entity.AllenException;
import com.allen.protocol.entity.MessageType;
import com.allen.protocol.entity.NettyMessage;
import com.allen.protocol.utils.SerializationUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AllenDBProxy {
    private volatile long leaderId;

    private final Map<Long, Node> nodes;

    public AllenDBProxy(String cluster) {
        nodes = new HashMap<>();
        String[] strNodes = cluster.split(",");
        for (int i = 0; i < strNodes.length; i++) {
            String[] idAddress = strNodes[i].split("@");
            String[] hostPort = idAddress[1].split(":");
            Node node = new Node(Long.valueOf(idAddress[0]), hostPort[0], Integer.valueOf(hostPort[1]), 8);
            nodes.put(node.getId(), node);
        }
    }

    private AllenClient conn(long nodeId) throws Exception {
        Node node = nodes.get(nodeId);
        if (node == null) {
            return null;
        }
        AllenClient client = node.getClient();
        if (!client.isConnected()) {
            client.connect();
        }
        return client;
    }

    public DataVO get(String key) throws Exception {
        Command command = new Command();
        command.setType(Command.Type.GET);
        command.setOperates(Arrays.asList(dataVO(key, null)));
        Object body = request(leaderId, command);
        Command c = SerializationUtils.read((byte[]) body, Command.class);
        return CollectionUtils.isEmpty(c.getOperates()) ? null : c.getOperates().get(0);
    }

    public String set(String key, Object value) throws Exception {
        Command command = new Command();
        command.setType(Command.Type.SET);
        command.setOperates(Arrays.asList(dataVO(key, value)));
        Object body = request(leaderId, command);
        String result = new String((byte[]) body);
        if (!"success".equals(result)) {
            throw new AllenException(result);
        }
        return result;
    }

    private Object request(long nodeId, Command command) throws Exception {
        AllenClient leader = conn(nodeId);
        HashMap<String, String> att = new HashMap<>();
        att.put(Constants.NETTY_MESSAGE_HEADER_ATTR_BODY_TYPE, BusMsgType.REQ_CLIENT.ordinal() + "");
        NettyMessage res = leader.request(att, command);
        NettyMessage.Header header = res.getHeader();
        if (header.getType() == MessageType.REDIRECT.code()) {
            leaderId = Long.valueOf(header.getAttribute(Constants.NETTY_MESSAGE_HEADER_ATTR_REDIRECT_PATH));
            return request(leaderId, command);
        }
        return res.getBody();
    }

    private DataVO dataVO(String key, Object value) {
        return new DataVO(key, value);
    }
}
