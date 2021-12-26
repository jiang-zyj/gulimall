package com.zyj.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zyj.common.utils.HttpUtils;
import com.zyj.common.utils.R;
import com.zyj.common.vo.MemberRespVo;
import com.zyj.gulimall.auth.feign.MemberFeignService;
import com.zyj.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName OAuth2Controller
 * @author: YaJun
 * @Date: 2021 - 12 - 25 - 16:04
 * @Package: com.zyj.gulimall.auth.controller
 * @Description: 处理社交登录请求
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/auth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {

        Map<String, String> map = new HashMap<>();
        // 1. 根据code换取accessToken
        map.put("client_id", "2640985139");
        map.put("client_secret", "b00a5817cdc7743f9b3e417ec63ef4de");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/auth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());
        // 处理accessToken
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            // 知道当前是哪个社交用户
            // 1. 当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员）
            // 登录或者注册这个社交用户
            R oauthLogin = memberFeignService.login(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功：用户信息 {}", data.toString());
                // TODO: 分布式session共享问题：
                //  1. 子父域不共享session数据
                //  2. 统一服务的不同服务器session数据不共享
                // TODO: 分布式session解决方案：
                //  可行的有：
                //  1. hash一致性：对ip进行哈希（但不利于水平扩展）
                //  2. 统一存储：对session的数据进行统一存储。（增加了一次网络IO，需要额外维护一个NOSQL中间件/集群）
                // TODO: 实际解决方案：
                //  1. 使用SpringSession解决session统一存储，使不同服务器能够共享session
                //  2. 解决子父域不共享session问题
                //  3. 另外，使用JSON存储session数据，而不使用JDK的序列化方式，增强session数据通用性 地址：https://github.com/spring-projects/spring-session/tree/2.5.4/spring-session-samples/spring-session-sample-boot-redis-json
                // 1. 第一次使用session；命令浏览器保存卡号。JSESSIONID这个cookie
                // 以后浏览器访问哪个网站就会带上这个网站的cookie；
                // 子域之间：gulimall.com    auth.gulimall.com   order.gulimall.com
                // 发卡的时候(指定域名为父域名)，即使是子域系统发的卡，也能让父域直接使用
                session.setAttribute("loginUser", data);
                //new Cookie("JSESSIONID", "xxx").setDomain();
                //servletResponse.addCookie(new Cookie("JSESSIONID", "xxx"));

                // 2. 登录成功就跳回首页
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
