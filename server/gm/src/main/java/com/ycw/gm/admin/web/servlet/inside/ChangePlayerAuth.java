package com.ycw.gm.admin.web.servlet.inside;

import com.ycw.gm.admin.domain.GmServer;
import com.ycw.gm.admin.web.servlet.inside.anno.FuncName;
import com.ycw.gm.common.utils.ParamParseUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

/**
 * @author tree
 * @date 2022/2/18 16:40
 */
@FuncName(name = "auth_set")
public class ChangePlayerAuth extends InsideServlet{
    @Override
    public String process(Map<String, Object> map) throws Exception {
        log.error("context data: {}", map);
//		Validate.isTrue(map.containsKey("act"), "act not exists");
        Validate.isTrue(map.containsKey("sid"), "rid not exists");
        Validate.isTrue(map.containsKey("rid"), "rid not exists");
        Validate.isTrue(map.containsKey("auth"), "auth not exists");

        try {
            String pid = map.containsKey("pid") ? castToString(map.get("pid")) : "-1";
            String sid = castToString(map.get("sid"));
            List<GmServer> gsrvs = gsrvs(pid, sid);
            map.put("cmd", "PlayerAuthChange");
            map.put("onlyOnce", "false");
            for (GmServer gsrv : gsrvs) {
                String url = ParamParseUtils.makeURL(gsrv.getInHost(), gsrv.getInPort(), "script");
                try {
                    ParamParseUtils.sendSyncTokenPost(url, map);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return fail(1, e.getMessage()).toString();
        }

        return succ().toJSONString();
    }
}