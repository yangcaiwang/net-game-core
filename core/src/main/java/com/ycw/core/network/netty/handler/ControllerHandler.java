package com.ycw.core.network.netty.handler;

import com.ycw.core.internal.thread.pool.actor.ActorThreadPoolExecutor;
import com.ycw.core.network.grpc.GrpcManager;
import com.ycw.core.network.netty.message.IMessage;
import com.ycw.core.network.netty.message.ProtoMessage;
import com.ycw.core.network.netty.method.WebsocketCmdContext;
import com.ycw.core.network.netty.method.WebsocketCmdParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <后端控制器处理器实现类>
 * <p>
 * ps: 接收路由器的protobuf消息，把消息转发到服务器的控制器
 *
 * @author <yangcaiwang>
 * @version <1.0>
 */
public class ControllerHandler implements ControllerListener {

    private static final Logger log = LoggerFactory.getLogger(ControllerHandler.class);
    public static final ActorThreadPoolExecutor actorExecutor = new ActorThreadPoolExecutor("controller-message-thread", Runtime.getRuntime().availableProcessors() * 2 + 1);

    @Override
    public void exec(IMessage msg) {
        WebsocketCmdParams websocketCmdParams = WebsocketCmdContext.getInstance().getMethodHandler(msg.getCmd());
        if (websocketCmdParams == null) {
            log.error("not found cmd:{} {} methodHandler", msg.getCmd(), msg);
            return;
        }

        WebsocketControllerHandler handlerMethodClass = WebsocketCmdContext.getInstance().getHandlerMethodClass(websocketCmdParams.getMethod().getDeclaringClass().getSimpleName());
        if (handlerMethodClass == null) {
            log.error("not found cmd:{} handle class", msg.getCmd());
            return;
        }

        IMessage resp = handlerMethodClass.process(websocketCmdParams, msg);
        if (resp != null) {
            GrpcManager.getInstance().sentRouter((ProtoMessage) resp);
        }
    }
}