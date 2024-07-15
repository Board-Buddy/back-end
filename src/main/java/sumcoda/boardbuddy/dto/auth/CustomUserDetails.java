package sumcoda.boardbuddy.dto.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sumcoda.boardbuddy.dto.AuthResponse;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final AuthResponse.ProfileDTO profileDTO;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        grantedAuthorities.add((GrantedAuthority) () -> profileDTO.getMemberRole().getValue());

        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return profileDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return profileDTO.getUsername();
    }

    // 사용자의 아이디가 만료되었는지
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 사용자의 아이디가 잠겨있는지
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 사용자의 아이디가 사용 가능한지
    @Override
    public boolean isCredentialsNonExpired() {
        return true;

    }

    // 사용자의 아이디가 사용 가능한지
    @Override
    public boolean isEnabled() {
        return true;
    }
}