package com.eat.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eat.reggie.common.BaseContext;
import com.eat.reggie.common.R;
import com.eat.reggie.entity.User;
import com.eat.reggie.service.UserService;
import com.eat.reggie.utils.SMSUtils;
import com.eat.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            String[] datas = new String[2];
            datas[0] = code;
            datas[1] = "1";

            //调用短信服务api完成发送短信
            SMSUtils.sendMessage(phone, datas);

            //需要将生成的验证码保存到session
            session.setAttribute(phone, code);

            return R.success("手机验证码短信发送成功");
        }


        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info("map:{}", map);

        //获取手机号
        String phone = (String)map.get("phone");

        //获取验证码
        String codeInput = (String) map.get("code");

        //从session中获取保存的验证码
        String code = (String) session.getAttribute(phone);

        //进行验证码的比对（页面提交的验证码和session中保存的验证码比对）
        if(code != null && code.equals(codeInput)){
            //如果能够比对成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            //判断当前手机号对应用户是否为新用户，如果是新用户就自动完成注册
            if(user == null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",  user.getId());
            return R.success(user);

        }


        return R.error("用户登录失败");
    }

    /**
     * 移动端用户退出
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        //清理session中保存的当前登录用户的id
        session.removeAttribute("user");
        return R.success("退出成功");
    }

}
