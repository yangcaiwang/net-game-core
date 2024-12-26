package com.common.module.network.jetty;

import com.common.module.cluster.enums.ServerType;
import com.common.module.internal.heart.jetty.JettyHeartbeatProcess;
import com.common.module.network.jetty.handler.JettyHttpHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <Jetty服务端启动类>
 * <p>
 *
 * @author <yangcaiwang>
 * @version <1.0>
 */
public class JettyHttpServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Server jettyServer;

    private JettyHeartbeatProcess jettyHeartbeatProcess;

    private static JettyHttpServer jettyHttpServer = new JettyHttpServer();

    public static JettyHttpServer getInstance() {
        return jettyHttpServer;
    }

    public void start(JettyHttpHandler jettyHttpHandler, Map<String, Integer> map, ServerType serverType) throws Exception {
        int port = map.get("port");
        int httpMinThreads =  map.get("httpMinThreads");
        int httpMaxThreads = map.get("httpMaxThreads");
        int idleTimeout =  map.get("idleTimeout");
        int heartbeatTime =  map.get("heartbeatTime");
        int heartbeatTimeout = map.get("heartbeatTimeout");
        jettyServer = new Server(getQueuedThreadPool(httpMinThreads, httpMaxThreads, idleTimeout));
        jettyServer.setDumpAfterStart(false);
        jettyServer.setDumpBeforeStop(false);
        jettyServer.setStopAtShutdown(false);
        jettyServer.addConnector(httpConnector(port, idleTimeout));
        jettyServer.setHandler(jettyHttpHandler);
        jettyServer.start();

        jettyHeartbeatProcess = new JettyHeartbeatProcess(heartbeatTime, heartbeatTimeout);
        if (serverType == ServerType.GM_SERVER) {
            jettyHeartbeatProcess.monitor();
        } else {
            jettyHeartbeatProcess.sent();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private QueuedThreadPool getQueuedThreadPool(int httpMinTreads, int httpMaxThreads, int idleTimeout) {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(httpMinTreads);
        threadPool.setMaxThreads(httpMaxThreads);
        threadPool.setIdleTimeout(idleTimeout);
        return threadPool;
    }

    private ServerConnector httpConnector(int port, int idleTimeout) {
        // 接受连接线程 与 连接事件处理线程都设置为1
        ServerConnector connector = new ServerConnector(jettyServer, 1, 2, new HttpConnectionFactory(getHttpConfig(port)));
        connector.setPort(port);
        connector.setIdleTimeout(idleTimeout);
        return connector;
    }

    private HttpConfiguration getHttpConfig(int port) {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("http");
        http_config.setSecurePort(port);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(true);
        return http_config;
    }

    private void stop() {
        try {
            if (jettyServer != null && jettyServer.isRunning()) {
                jettyServer.stop();

            }

            if (jettyHeartbeatProcess != null) {
                jettyHeartbeatProcess.showdown();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public JettyHeartbeatProcess getJettyHeartbeatProcess() {
        return jettyHeartbeatProcess;
    }
}