package myapp.backend.domain.auth.vo;

import java.time.LocalDateTime;

public class UserVO {

    private int user_id;
    private String sns_type;
    private String sns_id;
    private String username;
    private String email;
    private String profile_img;
    private String user_status;
    private String sanction_reason;
    private String sanction_start_date;
    private String sanction_end_date;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public UserVO() {
    }

    public UserVO(String sns_type, String sns_id, String username, String email, String profile_img) {
        this.sns_type = sns_type;
        this.sns_id = sns_id;
        this.username = username;
        this.email = email;
        this.profile_img = profile_img;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getSns_type() {
        return sns_type;
    }

    public void setSns_type(String sns_type) {
        this.sns_type = sns_type;
    }

    public String getSns_id() {
        return sns_id;
    }

    public void setSns_id(String sns_id) {
        this.sns_id = sns_id;
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

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public String getUser_status() {
        return user_status;
    }

    public void setUser_status(String user_status) {
        this.user_status = user_status;
    }

    public String getSanction_reason() {
        return sanction_reason;
    }

    public void setSanction_reason(String sanction_reason) {
        this.sanction_reason = sanction_reason;
    }

    public String getSanction_start_date() {
        return sanction_start_date;
    }

    public void setSanction_start_date(String sanction_start_date) {
        this.sanction_start_date = sanction_start_date;
    }

    public String getSanction_end_date() {
        return sanction_end_date;
    }

    public void setSanction_end_date(String sanction_end_date) {
        this.sanction_end_date = sanction_end_date;
    }

    @Override
    public String toString() {
        return "UserVO{" +
               "user_id=" + user_id +
               ", sns_type='" + sns_type + '\'' +
               ", sns_id='" + sns_id + '\'' +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", profile_img='" + profile_img + '\'' +
               ", created_at=" + created_at +
               ", updated_at=" + updated_at +
               '}';
    }
} 