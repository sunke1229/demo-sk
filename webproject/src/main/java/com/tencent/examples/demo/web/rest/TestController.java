package com.tencent.examples.demo.web.rest;

import com.tencent.bk.api.cc.CCApi;
import com.tencent.bk.api.cc.model.CommonSearchDataList;
import com.tencent.bk.api.cc.req.SearchBusinessReq;
import com.tencent.bk.api.cc.req.SearchHostReq;
import com.tencent.bk.api.gse.GseApi;
import com.tencent.bk.api.gse.model.AgentStatus;
import com.tencent.bk.api.gse.model.Host;
import com.tencent.bk.api.gse.req.GetAgentStatusReq;
import com.tencent.bk.api.job.JobApi;
import com.tencent.bk.api.job.model.Job;
import com.tencent.bk.api.job.req.GetJobListReq;
import com.tencent.bk.api.paas.BKPaaSApi;
import com.tencent.bk.api.paas.model.GetAppInfo;
import com.tencent.bk.api.paas.req.GetAppInfoReq;
import com.tencent.bk.api.protocol.ApiResp;
import com.tencent.bk.core.BkConsts;
import com.tencent.bk.core.dto.cmdb.ApplicationDto;
import com.tencent.bk.core.dto.cmdb.HostDto;
import com.tencent.bk.core.dto.login.BkUserDto;
import com.tencent.bk.core.sdk.cmdb.CMDBClient;
import com.tencent.bk.core.sdk.cmdb.protocol.GetAppByUserReq;
import com.tencent.bk.core.sdk.cmdb.protocol.GetAppHostListReq;
import com.tencent.bk.core.sdk.paas.PAASClient;
import com.tencent.bk.core.sdk.paas.protocol.GetBatchUserReq;
import com.tencent.bk.core.sdk.protocol.ESBReq;
import com.tencent.bk.core.sdk.protocol.ESBResp;
import com.tencent.bk.sdk.web.filter.util.FilterUtil;
import com.tencent.bk.utils.json.JsonUtil;
import com.tencent.bk.utils.string.StringUtil;
import com.tencent.examples.demo.model.*;
import com.tencent.examples.helper.RespHelper;
import com.tencent.examples.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TestController extends BaseController {

    @Autowired
    private CMDBClient cmdbClient;
    @Autowired
    private PAASClient paasClient;
    @Autowired
    private JobApi jobApi;
    @Autowired
    private BKPaaSApi bkPaaSApi;
    @Autowired
    private CCApi ccApi;
    @Autowired
    private GseApi gseApi;


    @RequestMapping("/business/list")
    @ResponseBody
    public String demo() {
        SearchBusinessReq req = ccApi.makeBaseReqByWeb(SearchBusinessReq.class,request);
        return   JsonUtil.toJson(ccApi.searchBusiness(req));
    }


    @RequestMapping("/onlineHosts")
    @ResponseBody
    public String onlineHosts() {
        SearchHostReq req = ccApi.makeBaseReqByWeb(SearchHostReq.class,request);
        List<Map<String, Object>> list =  ccApi.searchHost(req).getData().getInfo();
        Map<String,Map<String, Object>> hostMap = new HashMap();
        Set<Host> hosts = new HashSet<>();
        GetAgentStatusReq gseReq = gseApi.makeBaseReqByWeb(GetAgentStatusReq.class,request);
        gseReq.setBkSupplierId(0);
        for(Map<String, Object> map :list){
            Map<String,Object> hostInfo = (Map<String,Object>) map.get("host");
            String ip = (String)hostInfo.get("bk_host_innerip");
            List clouds = (List) hostInfo.get("bk_cloud_id");
            Map<String,Object> cloudIdInfo = ( Map<String,Object>) clouds.get(0);
            String cloudId = (String)cloudIdInfo.get("id");
            Host host = new Host();
            host.setIp(ip);
            host.setBkCloudId(Integer.parseInt(cloudId));
            hosts.add(host);

            hostMap.put(ip,map);
        }
        gseReq.setHosts(hosts);
        Map<String, AgentStatus> agentStatusReq  = gseApi.getAgentStatus(gseReq).getData();
        for(AgentStatus agentStatus :agentStatusReq.values()){
            if(agentStatus.getAlive()==0){
                hostMap.remove(agentStatus.getIp());
            }
        }
        return   JsonUtil.toJson(RespHelper.ok(hostMap.values()));
    }


}
