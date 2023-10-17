package com.example.barberstaffapplication.Model;

import com.example.barberstaffapplication.Common.Common;

public class MyToken {
    private String token,userPhone;
    private Common.TOKEN_TYPE tokenType;

    public MyToken(String token, String userPhone, Common.TOKEN_TYPE tokenType) {
        this.token = token;
        this.userPhone = userPhone;
        this.tokenType = tokenType;
    }

    public MyToken() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Common.TOKEN_TYPE getTokenType() {
        return tokenType;
    }

    public void setTokenType(Common.TOKEN_TYPE tokenType) {
        this.tokenType = tokenType;
    }
}
