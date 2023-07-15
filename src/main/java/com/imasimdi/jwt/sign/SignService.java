package com.imasimdi.jwt.sign;

import com.imasimdi.jwt.sercurity.JwtProvider;
import com.imasimdi.jwt.sercurity.Token;
import com.imasimdi.jwt.sercurity.TokenDto;
import com.imasimdi.jwt.sercurity.TokenRepository;
import com.imasimdi.jwt.sign.dto.SignRequest;
import com.imasimdi.jwt.sign.dto.SignResponse;
import com.imasimdi.jwt.sign.dto.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SignService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    public SignResponse login(SignRequest request) throws Exception {
        Member member = memberRepository.findByAccount(request.getAccount()).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("잘못된 계정정보입니다.");
        }

        return SignResponse.builder()
                .id(member.getId())
                .account(member.getAccount())
                .name(member.getName())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .roles(member.getRoles())
                .token(TokenDto.builder()
                        .access_token(jwtProvider.createToken(member.getAccount(), member.getRoles()))
                        .refresh_token(member.getRefreshToken())
                        .build())
                .build();
    }

    public boolean register(SignRequest request) throws Exception {
        try {
            Member member = Member.builder()
                    .account(request.getAccount())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .nickname(request.getNickname())
                    .email(request.getEmail())
                    .build();

            member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
            memberRepository.save(member);

            String refreshToken = createRefreshToken(member);
            member.setRefreshToken(refreshToken);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("잘못된 요청입니다.");
        }
        return true;
    }

    public UserDetailResponse getMember(String account) throws Exception {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new Exception("계정을 찾을 수 없습니다."));
        return new UserDetailResponse(member);
    }
    // Refresh Token ================

    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:memberId : tokenValue
     * 형태로 저장한다.
     */
    public String createRefreshToken(Member member) {
        System.out.println(member.getId());
        Token token = tokenRepository.save(
                Token.builder()
                        .id(member.getId())
                        .refresh_token(UUID.randomUUID().toString())
                        .expiration(1)
                        .build()
        );
        return token.getRefresh_token();
    }
    //고쳐야함
    public Optional<Token> validRefreshToken(Member member, String refreshToken) throws Exception {
        Optional<Token> token = tokenRepository.findById(member.getId());
        // 해당유저의 Refresh 토큰 만료 : Redis에 해당 유저의 토큰이 존재하지 않음
        if (token == null) {
            return null;
        } else {
            // 토큰이 같은지 비교
            if token.get().getRefresh_token()
            return token;
            }
    }

    public TokenDto refreshAccessToken(TokenDto token) throws Exception {
        String account = jwtProvider.getAccount(token.getAccess_token());
        Member member = memberRepository.findByAccount(account).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));

        Optional<Token> refreshToken = validRefreshToken(member, token.getRefresh_token());
        if (refreshToken == null) {
            member.setRefreshToken(createRefreshToken(member));
        }
            return TokenDto.builder()
                    .access_token(jwtProvider.createToken(account, member.getRoles()))
                    .refresh_token(refreshToken.get().getRefresh_token())
                    .build();
        }
    }
