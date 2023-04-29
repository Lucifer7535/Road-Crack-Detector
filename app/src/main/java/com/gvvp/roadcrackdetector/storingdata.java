package com.gvvp.roadcrackdetector;

public class storingdata {
    String username, fullname, email, phoneno;

    public storingdata() {
    }

    public storingdata(String username, String fullname, String email, String phoneno) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.phoneno = phoneno;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

}
