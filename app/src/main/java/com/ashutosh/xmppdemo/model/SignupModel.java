package com.ashutosh.xmppdemo.model;

public class SignupModel {

    String mUsername, mPassword, mConfirmPassword;

    public SignupModel(String mUsername, String mPassword, String mConfirmPassword) {
        this.mUsername = mUsername;
        this.mPassword = mPassword;
        this.mConfirmPassword = mConfirmPassword;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public boolean checkPassword() {
        return ( !this.mPassword.isEmpty() && (this.mPassword.equals(this.mConfirmPassword)));
    }

    public boolean validateFields(){
        return ( !this.mUsername.isEmpty() && !this.mPassword.isEmpty() );
    }
}
