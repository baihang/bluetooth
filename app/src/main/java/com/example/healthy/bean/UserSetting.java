package com.example.healthy.bean;

public class UserSetting {

    public String userName;
    public String vk;
    public String token;
    public String pk;
    public String userId;
    public Long tokenTime;
    public String apiServer;

    @Override
    public String toString() {
        return "UserSetting{" +
                "userName='" + userName + '\'' +
                ", vk='" + vk + '\'' +
                ", token='" + token + '\'' +
                ", pk='" + pk + '\'' +
                ", userId='" + userId + '\'' +
                ", tokenTime=" + tokenTime +
                ", apiServer='" + apiServer + '\'' +
                '}';
    }
}
