package com.swiot.web.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.swiot.common.annotation.Anonymous;
import com.swiot.common.core.domain.entity.SysUser;
import com.swiot.common.core.domain.model.LoginBody;
import com.swiot.common.core.domain.model.RegisterBody;
import com.swiot.common.utils.sign.RsaUtils;
import com.swiot.framework.web.service.SysLoginService;
import com.swiot.framework.web.service.SysRegisterService;
import org.apache.catalina.User;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;
import com.swiot.common.config.RuoYiConfig;
import com.swiot.common.constant.CacheConstants;
import com.swiot.common.constant.Constants;
import com.swiot.common.core.domain.AjaxResult;
import com.swiot.common.core.redis.RedisCache;
import com.swiot.common.utils.sign.Base64;
import com.swiot.common.utils.uuid.IdUtils;
import com.swiot.system.service.ISysConfigService;

/**
 * 验证码操作处理
 * 
 * @author SuGuichuan
 */
@RestController
public class CaptchaController
{
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SysRegisterService registerService;
    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException
    {
        AjaxResult ajax = AjaxResult.success();
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled)
        {
            return ajax;
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        // 生成验证码
        String captchaType = RuoYiConfig.getCaptchaType();
        if ("math".equals(captchaType))
        {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        }
        else if ("char".equals(captchaType))
        {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            return AjaxResult.error(e.getMessage());
        }

        ajax.put("uuid", uuid);
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }
    @Anonymous
    @GetMapping("/dynamic-code")
    public AjaxResult generateDynamicCode(@RequestParam String openid) {
        System.out.println("openid:"+openid);
        // 1. 生成4位随机验证码（0000-9999）
        String code = String.format("%04d", new Random().nextInt(10000));
        String key="sk"+code;
        JSONObject dynamicObject=new JSONObject();
        dynamicObject.put("code",code);
        dynamicObject.put("openid",openid);
        // 3. 存入Redis（5分钟有效期）
        redisCache.setCacheObject(key, dynamicObject, 5, TimeUnit.MINUTES);

        // 4. 返回结果（生产环境应移除code字段）
        return AjaxResult.success()
                .put("dynamic", code)
                .put("expire", 5);  // 过期时间（分钟）
    }
    @Anonymous
    @GetMapping("/dynamic-login")
    public AjaxResult dynamicLogin(@RequestParam String code) throws Exception {
        String key="sk"+code;


        Boolean hasKey= redisCache.hasKey(key);
        AjaxResult ajax =AjaxResult.error();
        if(hasKey){
            //如果码存在获取用户名
            JSONObject dynamicObject=redisCache.getCacheObject(key);
            String  username= dynamicObject.getString("openid");
            String  nickname= "微信用户";
            RegisterBody registerBody=new RegisterBody();
            registerBody.setUsername(username);
            registerBody.setNickname(nickname);
            registerBody.setPassword(username);
            String msg = registerService.register(registerBody);
            System.out.println(msg);
            //先检测用户是否存在

            ajax = AjaxResult.success();


            //根据用户名提取密码

        // 生成令牌
        String token = loginService.login(registerBody.getUsername(), registerBody.getPassword(), registerBody.getCode(),
                registerBody.getUuid());
        ajax.put(Constants.TOKEN, token);
            redisCache.deleteObject(key);
        return ajax;
        }
        return ajax;
    }
}
