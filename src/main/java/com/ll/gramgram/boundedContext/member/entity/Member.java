package com.ll.gramgram.boundedContext.member.entity;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // @CreatedDate, @LastModifiedDate 작동하게 허용
@ToString
@Entity // 아래 클래스는 member 테이블과 대응되고, 아래 클래스의 객체는 테이블의 row와 대응된다.
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;
    private String providerTypeCode;
    @Column(unique = true)
    private String username;
    private String password;
    @OneToOne
    @Setter
    private InstaMember instaMember;

    public List<? extends GrantedAuthority> getGrantedAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        grantedAuthorities.add(new SimpleGrantedAuthority("member"));

        if ("admin".equals(username)) {
            grantedAuthorities.add(new SimpleGrantedAuthority("admin"));
        }

        return grantedAuthorities;
    }

    public boolean hasConnectedInstaMember() {
        return instaMember != null;
    }

    public String getNickname() {
        return "%1$4s".formatted(Long.toString(getId(), 36)).replace(' ', '0');
    }
}