package com.yiheoline.qcloud.xiaozhibo.bean;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String avatar;
    private String birthday;
    private String cover;
    private String introduction;
    private String mobile;
    private String nickname;
    private String profession;
    private String residence;
    private int fanNum;
    private int followNum;
    private int gender;
    private String height;
    private int likes;
    private int totalWutongbi;
    private int totalWutongye;
    private int type;
    private String weight;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public int getFanNum() {
        return fanNum;
    }

    public void setFanNum(int fanNum) {
        this.fanNum = fanNum;
    }

    public int getFollowNum() {
        return followNum;
    }

    public void setFollowNum(int followNum) {
        this.followNum = followNum;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }


    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getTotalWutongbi() {
        return totalWutongbi;
    }

    public void setTotalWutongbi(int totalWutongbi) {
        this.totalWutongbi = totalWutongbi;
    }

    public int getTotalWutongye() {
        return totalWutongye;
    }

    public void setTotalWutongye(int totalWutongye) {
        this.totalWutongye = totalWutongye;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
