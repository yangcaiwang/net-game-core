package com.gm.server.admin.web.servlet.inside;

import com.gm.server.admin.domain.GmServer;
import com.gm.server.admin.web.servlet.inside.anno.FuncName;
import com.gm.server.common.utils.ParamParseUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

@FuncName(name = "gm_privilege")
public class GmPrivilege extends InsideServlet {

	@Override
	public String process(Map<String, Object> map) throws Exception {

		log.error("context data: {}", map);
		Validate.isTrue(map.containsKey("sid"), "sid not exists");
		String sid = castToString(map.get("sid"));
		String pid = map.containsKey("pid") ? castToString(map.get("pid")) : "-1";
		List<GmServer> gsrvs = gsrvs(pid, sid);
		if (gsrvs.isEmpty()) {
			return fail(-1, String.format("server %s not found", sid)).toJSONString();
		}
		map.put("cmd", "GmPrivilege");
		map.put("onlyOnce", "false");
		map.put("optType", 1);
		for (GmServer gsrv : gsrvs) {
			String url = ParamParseUtils.makeURL(gsrv.getInHost(), gsrv.getInPort(), "script");
			try {
				String result = ParamParseUtils.sendSyncTokenPost(url, map);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return succ().toJSONString();
	}
}
