package com.jhf.coupon.api.dto;

/**
 * Login Response DTO
 * Returned after successful authentication
 */
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo userInfo;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userInfo = userInfo;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * Nested class for user information
     */
    public static class UserInfo {
        private int userId;
        private String email;
        private String clientType;
        private String name; // Company name or Customer full name

        public UserInfo() {
        }

        public UserInfo(int userId, String email, String clientType, String name) {
            this.userId = userId;
            this.email = email;
            this.clientType = clientType;
            this.name = name;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getClientType() {
            return clientType;
        }

        public void setClientType(String clientType) {
            this.clientType = clientType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
