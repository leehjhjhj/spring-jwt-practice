package com.imasimdi.jwt.sign.dto;

import com.imasimdi.jwt.sercurity.TokenDto;
import com.imasimdi.jwt.sign.Authority;
import com.imasimdi.jwt.sign.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailResponse {

    private Long id;

    private String account;

    private String nickname;

    private String name;

    private String email;

    private List<Authority> roles = new ArrayList<>();

    private String token;

    public UserDetailResponse(Member member) {
        this.id = member.getId();
        this.account = member.getAccount();
        this.nickname = member.getNickname();
        this.name = member.getName();
        this.email = member.getEmail();
        this.roles = member.getRoles();
        this.token = member.getRefreshToken();
    }
}