package com.resale.platform.service;

import java.awt.image.BufferedImage;

/**
 * 验证码服务接口
 *
 * @author MiniMax Agent
 */
public interface CaptchaService {

    /**
     * 生成图形验证码
     *
     * @return 包含验证码图片和Key的数组 [BufferedImage, key]
     */
    Object[] generateCaptcha();

    /**
     * 验证图形验证码
     *
     * @param key 验证码Key
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    boolean verifyCaptcha(String key, String code);

    /**
     * 删除验证码
     *
     * @param key 验证码Key
     */
    void deleteCaptcha(String key);
}
