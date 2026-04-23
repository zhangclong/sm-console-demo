package com.uh.system.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.uh.framework.cache.ObjectCache;
import com.uh.common.exception.ServiceException;
import com.uh.common.utils.StringUtils;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;
import com.uh.common.config.ConsoleConfig;
import com.uh.common.constant.CacheConstants;
import com.uh.common.constant.Constants;
import com.uh.common.core.domain.AjaxResult;
import com.uh.util.Base64;
import com.uh.common.utils.uuid.IdUtils;

/**
 * 验证码操作处理
 *
 * @author XiaoZhangTongZhi
 */
@RestController
public class CaptchaController {

    private static final Integer captchaWidth = 115;

    private static final Integer captchaHeight = 42;

    @Autowired
    @Qualifier(value= "captchaProducer")
    private Producer captchaProducer;

    @Autowired
    @Qualifier(value= "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private ObjectCache redisCache;

    @Resource
    private ConsoleConfig consoleConf;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) {
        AjaxResult ajax = AjaxResult.success();

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;
        Captcha captcha = null;
        // 生成验证码
        String captchaType = consoleConf.getCaptchaType();
        if ("math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        } else if ("easy-math".equals(captchaType)) {
            captcha = new ArithmeticCaptcha(captchaWidth, captchaHeight);
        } else if ("easy-chinese".equals(captchaType)) {
            captcha = new ChineseCaptcha(captchaWidth, captchaHeight);
        } else if ("easy-char".equals(captchaType)) {
            captcha = new SpecCaptcha(captchaWidth, captchaHeight);
        } else {
            throw new ServiceException("user.jcaptcha.config.error");
        }

        ajax.put("uuid", uuid);

        // ry-math  ry-char
        if ("math".equals(captchaType) || "char".equals(captchaType)) {
            // 转换流信息写出
            FastByteArrayOutputStream os = new FastByteArrayOutputStream();
            try {
                ImageIO.write(image, "jpg", os);
            } catch (IOException e) {
                return AjaxResult.error(e.getMessage());
            }
            ajax.put("img", Base64.encode(os.toByteArray()));
        } else {

            code = captcha.text();

            if (StringUtils.isEmpty(code))
                throw new ServiceException("user.jcaptcha.generate.error");

            String base64Code = captcha.toBase64();

            ajax.put("img", base64Code.split(",")[1]);
        }

        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        return ajax;

    }
}
