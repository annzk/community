package com.majiang.community.Controller;

import com.majiang.community.dto.AccessTokenDTO;
import com.majiang.community.dto.GithubUser;
import com.majiang.community.mapper.UserMapper;
import com.majiang.community.model.User;
import com.majiang.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;
    @Autowired
    private UserMapper userMapper;
    @Value("${Github.client.id}")
    private String client_id;
    @Value("${Github.client.secret}")
    private String client_secret;
    @Value("${Github.redirect.uri}")
    private String redirect_uri;
   @GetMapping("/callback")
    public String callback(@RequestParam(name ="code")String code,
                           @RequestParam(name ="state")String state,
                           HttpServletRequest request, HttpServletResponse response){
       AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
       accessTokenDTO.setClient_id(client_id);
       accessTokenDTO.setState(state);
       accessTokenDTO.setClient_secret(client_secret);
       accessTokenDTO.setCode(code);
       accessTokenDTO.setRedirect_uri(redirect_uri);
       String accessToken = githubProvider.getAccessToken(accessTokenDTO);
       GithubUser githubUser = githubProvider.getUser(accessToken);
       if(githubUser!=null){
           //登录成功
           //把token 拿出来生产cookie传输到客户端去，
           String token=UUID.randomUUID().toString();
           User user=new User();
           user.setAccountId(String.valueOf(githubUser.getId()));
           user.setGmtCreate(System.currentTimeMillis());
           user.setGmtModeify(user.getGmtCreate());
           user.setName(githubUser.getName());
           user.setToken(token);
           response.addCookie(new Cookie("token",token));
           userMapper.insert(user);

           return  "redirect:/";
       }else {
           //登录失败
           return  "redirect:/";
       }
   }
}
