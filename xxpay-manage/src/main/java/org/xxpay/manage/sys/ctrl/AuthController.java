package org.xxpay.manage.sys.ctrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.MenuTreeBuilder;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.CookieUtil;
import org.xxpay.core.common.util.RandomValidateCodeUtil;
import org.xxpay.core.common.util.StrUtil;
import org.xxpay.core.entity.SysResource;
import org.xxpay.core.entity.SysUser;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.secruity.JwtAuthenticationRequest;
import org.xxpay.manage.sys.service.SysUserService;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH)
@RestController
public class AuthController extends BaseController {

    @Value("${jwt.cookie}")
    private String tokenCookie;

    @Value("${jwt.expiration}")
    private Integer expiration;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * ????????????
     * @return
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/auth")
    @MethodLog( remark = "??????" )
    public ResponseEntity<?> auth(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException{
        JSONObject param = getJsonParam(request);
        String username = getStringRequired(param, "username");
        String password = getStringRequired(param, "password");
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest(username, password);
        String token;
        try {
           token = sysUserService.login(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        }catch (ServiceException e) {
            return ResponseEntity.ok(BizResponse.build(e.getRetEnum()));
        }
        JSONObject data = new JSONObject();
        data.put("access_token", token);
        // ??????cookie
        Cookie cookie = new Cookie(tokenCookie, token);
        cookie.setPath("/");
        //cookie.setDomain("xxpay.org");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(expiration);// ???
        response.addCookie(cookie);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(data));
    }

    /**
     * ??????token
     * @param request
     * @return
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/refresh")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(
            HttpServletRequest request, HttpServletResponse response) throws AuthenticationException{
        String token = CookieUtil.getCookieByName(request, tokenCookie);
        String refreshedToken;
        try {
            refreshedToken = sysUserService.refreshToken(token);
        }catch (ServiceException e) {
            return ResponseEntity.ok(BizResponse.build(e.getRetEnum()));
        }
        if(refreshedToken == null) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        } else {
            JSONObject data = new JSONObject();
            data.put("token", token);
            // ??????cookie
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");
            //cookie.setDomain("xxpay.org");
            cookie.setMaxAge(expiration);// ???
            response.addCookie(cookie);
            return ResponseEntity.ok(XxPayResponse.buildSuccess(data));
        }
    }

    /**
     * ??????????????????????????????
     * @return
     */
    @RequestMapping("/pwd_update")
    @ResponseBody
    @MethodLog( remark = "????????????" )
    public ResponseEntity<?> updatePassword(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        // ?????????
        String oldRawPassword = getStringRequired(param, "oldPassWord");
        // ?????????
        String rawPassword = getStringRequired(param, "passWord");
        // ???????????????????????????
        SysUser sysUser = rpcCommonService.rpcSysService.findByUserId(getUser().getId());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(!encoder.matches(oldRawPassword, sysUser.getPassWord())) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MCH_OLDPASSWORD_NOT_MATCH));
        }
        // ?????????????????????
        if(!StrUtil.checkPassword(rawPassword)) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MCH_PASSWORD_FORMAT_FAIL));
        }
        sysUser = new SysUser();
        sysUser.setUserId(getUser().getId());
        sysUser.setPassWord(encoder.encode(rawPassword));
        sysUser.setLastPasswordResetTime(new Date());
        int count = rpcCommonService.rpcSysService.update(sysUser);
        if(count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }

    /**
     * ????????????????????????
     * @return
     */
    @RequestMapping("/current")
    @ResponseBody
    public ResponseEntity<?> current() {
        SysUser sysUser = sysUserService.findByUserId(getUser().getId());
        return ResponseEntity.ok(XxPayResponse.buildSuccess(sysUser));
    }

    /**
     * ??????????????????
     * @param request
     * @return
     */
    @RequestMapping("/menu")
    @ResponseBody
    public ResponseEntity<?> menu(HttpServletRequest request) {
        List<SysResource> sysResourceList;
        if(MchConstant.PUB_YES == getUser().getIsSuperAdmin()) {
            // ???????????????????????????,???????????????????????????
            sysResourceList = rpcCommonService.rpcSysService.selectAllResource(MchConstant.SYSTEM_MGR);
        }else {
            // ???????????????????????????
            sysResourceList = rpcCommonService.rpcSysService.selectResourceByUserId(getUser().getId());
        }
        List<MenuTreeBuilder.Node> nodeList = new LinkedList<>();
        for(SysResource sysResource : sysResourceList) {
            MenuTreeBuilder.Node node = new MenuTreeBuilder.Node();
            node.setResourceId(sysResource.getResourceId());
            node.setName(sysResource.getName());
            node.setTitle(sysResource.getTitle());
            if(StringUtils.isNotBlank(sysResource.getJump())) node.setJump(sysResource.getJump());
            if(StringUtils.isNotBlank(sysResource.getIcon())) node.setIcon(sysResource.getIcon());
            node.setParentId(sysResource.getParentId());
            nodeList.add(node);
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(JSONArray.parseArray(MenuTreeBuilder.buildTree(nodeList))));
    }

    /**
     * ???????????????
     */
    @RequestMapping(value = "/auth/auth_code_get")
    public void getAuthCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map randomMap = RandomValidateCodeUtil.getRandcode(120, 40, 6, 20);
        // TODO ??????????????????,????????????
        String randomString = randomMap.get("randomString").toString();
        BufferedImage randomImage = (BufferedImage)randomMap.get("randomImage");
        ImageIO.write(randomImage, "JPEG", response.getOutputStream());
    }

}
