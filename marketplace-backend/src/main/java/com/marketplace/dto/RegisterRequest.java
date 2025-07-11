package com.marketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Định dạng email không hợp lệ")
    @Pattern(
        regexp = "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$",
        message = "Email phải có định dạng hợp lệ với tên miền đầy đủ"
    )
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số"
    )
    private String password;

    @NotBlank(message = "Tên là bắt buộc")
    @Size(min = 2, max = 50, message = "Tên phải từ 2-50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc") 
    @Size(min = 2, max = 50, message = "Họ phải từ 2-50 ký tự")
    private String lastName;

    @Pattern(
        regexp = "^(0|\\+84)[3-9]\\d{8}$",
        message = "Số điện thoại phải là số Việt Nam hợp lệ"
    )
    private String phone;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}