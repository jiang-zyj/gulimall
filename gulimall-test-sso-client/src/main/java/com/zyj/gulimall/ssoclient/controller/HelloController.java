package com.zyj.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName HelloController
 * @author: YaJun
 * @Date: 2021 - 12 - 26 - 17:38
 * @Package: com.zyj.gulimall.ssoclient.controller
 * @Description:
 * 单点登录核心：
 *  1. 给登录服务器留下登录痕迹（本示例中是给ssoserver存入cookie）
 *  2. 登录服务器要将token信息重定向的时候，带到url地址上
 *  3. 其他系统要处理url地址上的关键token，只要有，将token对应的用户保存到自己的session中
 *  4. 自己系统将用户保存在自己的会话中。
 *  5. 最后，这个功能可以抽取成一个filter，放到任何系统里面
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    private String ssoServerUrl;

    /**
     * 无需登录就可访问
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * 需要登录才可访问
     * 需要感知这次实在ssoserver登录成功跳转回来的，而不是我们直接访问的。
     * @param model
     * @param session
     * @param token 只要去 ssoserver 登录成功跳回来才会带上
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token", required = false) String token) {

        if (!StringUtils.isEmpty(token)) {
            // 去 ssoserver 登录成功跳回来才会带上
            // TODO：1、去ssoserver获取当前token真正对应的用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser", body);
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            // 没登录，跳转到登录服务器
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("张三");
            emps.add("李四");

            model.addAttribute("emps", emps);

            return "list";
        }

    }

}
