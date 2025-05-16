package com.swiot.common.core.domain.model;

/**
 * 用户登录对象
 * 
 * @author SuGuichuan
 */
public class LoginBody
{
    /**
     * 用户名
     */
    private String username;

    private String nickname;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;

    public String getUsername()
    {
        return username;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
