package com.ll.gramgram.base.security;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;
    private final InstaMemberService instaMemberService;
    private final Rq rq;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        if(providerTypeCode.equals("INSTAGRAM")){
            if (rq.isLogout()) {
                throw new OAuth2AuthenticationException("로그인 후 이용해주세요.");
            }

            String userInfoUri = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
            userInfoUri = userInfoUri.replace("{access-token}", userRequest.getAccessToken().getTokenValue());
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);
            Map<String, String> userAttributes = response.getBody();

            String gender = rq.getSessionAttr("connectByApi__gender", "W");
            rq.removeSessionAttr("connectByApi__gender");

            instaMemberService.connect(rq.getMember(), gender, userAttributes.get("id"), userAttributes.get("username"), userRequest.getAccessToken().getTokenValue());

            Member member = rq.getMember();
            return new CustomOAuth2User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
        }

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String oauthId = "";

        if(providerTypeCode.equals("NAVER")){
            Map<String, Object> map = oAuth2User.getAttributes();
            LinkedHashMap<String, String> getNaverId = (LinkedHashMap<String, String>) map.get("response"); // {id=dRGaNay80T4LoiuL8-0p15O-UhT31OFzV3JV3qOt8fA}
            oauthId = getNaverId.get("id");
        }

        else{
            Map<String, Object> map = oAuth2User.getAttributes();
            oauthId = oAuth2User.getName();
        }

        String username = providerTypeCode + "__%s".formatted(oauthId);

        Member member = memberService.whenSocialLogin(providerTypeCode, username).getData();

        return new CustomOAuth2User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
    }
}

class CustomOAuth2User extends User implements OAuth2User {

    public CustomOAuth2User(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}