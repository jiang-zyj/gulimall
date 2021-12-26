package com.zyj.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @program: gulimall
 * @ClassName LoginController
 * @author: YaJun
 * @Date: 2021 - 12 - 26 - 17:49
 * @Package: com.zyj.gulimall.ssoserver.controller
 * @Description:
 * 单点登录核心：
 *  1. 给登录服务器留下登录痕迹（本示例中是给ssoserver存入cookie）
 *  2. 登录服务器要将token信息重定向的时候，带到url地址上
 *  3. 其他系统要处理url地址上的关键token，只要有，将token对应的用户保存到自己的session中
 *  4. 自己系统将用户保存在自己的会话中。
 *  5. 最后，这个功能可以抽取成一个filter，放到任何系统里面
 *
 */
@Controller
public class LoginController {


    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token) {
        String userInfo = redisTemplate.opsForValue().get(token);
        return userInfo;
    }

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,
                            @CookieValue(value = "sso_token", required = false) String sso_token) {
        if (!StringUtils.isEmpty(sso_token)) {
            // 说明之前有人登陆过，浏览器留下来痕迹
            return "redirect:" + url + "?token=" + sso_token;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url,
                          HttpServletResponse response) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            // 登录成功跳转，跳转到之前的页面
            // 把登录成功的用户存起来
            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(uuid, username);
            Cookie cookie = new Cookie("sso_token", uuid);
            response.addCookie(cookie);
            return "redirect:" + url + "?token=" + uuid;
        } else {
            // 登录失败，展示登录页
            return "login";
        }



    }


}
