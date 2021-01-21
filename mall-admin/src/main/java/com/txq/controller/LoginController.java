package com.txq.controller;

import com.txq.commom.api.CommonResult;
import com.txq.dto.UmsAdminLoginParam;
import com.txq.service.UmsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
//@RestController
public class LoginController {
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    UmsAdminService umsAdminService;

    //@ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@Validated @RequestBody UmsAdminLoginParam umsAdminLoginParam) {
        String token = umsAdminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        if (token == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }
    /*@RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public APIResult login(@RequestBody Admin admin){
        Admin admin1 = loginService.adminLogin(admin.getUsername(), admin.getPassword());
        if (admin1 == null){
            return new APIResult(400, "账户或密码错误，登陆失败！");
        } else {
            return new APIResult(200,"登陆成功！", admin1);
        }
    }*/
}
