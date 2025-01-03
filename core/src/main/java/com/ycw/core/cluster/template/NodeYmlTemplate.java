package com.ycw.core.cluster.template;

/**
 * <集群节点解析yml模版类>
 * <p>
 *
 * @author <yangcaiwang>
 * @version <1.0>
 */
public class NodeYmlTemplate {

    /**
     * 服务器id
     */
    private String serverId;

    /**
     * 机器ip
     */
    private String host;

    /**
     * 端口号
     */
    private int port;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 组id
     */
    private int groupId;

    /**
     * 权重
     */
    private int weight;

    /**
     * 开服时间
     */
    private long openTime;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    @Override
    public String toString() {
        return "NodeYmlTemplate{" +
                "serverId='" + serverId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", serverName='" + serverName + '\'' +
                ", groupId=" + groupId +
                ", weight=" + weight +
                ", openTime=" + openTime +
                '}';
    }
}
