package com.eventsphere.common.security;

import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class EventSphereUserPrincipal implements UserDetails {

    private final String attendeeId;
    private final String eventId;
    private final String name;
    private final String email;
    private final UserRole role;
    private final AttendeeCategory category;
    private final String exhibitorId;
    private final List<GrantedAuthority> authorities;

    public EventSphereUserPrincipal(
            String attendeeId,
            String eventId,
            String name,
            String email,
            UserRole role,
            AttendeeCategory category,
            String exhibitorId
    ) {
        this.attendeeId = attendeeId;
        this.eventId = eventId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.category = category;
        this.exhibitorId = exhibitorId;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public AttendeeCategory getCategory() {
        return category;
    }

    public String getExhibitorId() {
        return exhibitorId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return ""; // Passwordless access flow
    }

    @Override
    public String getUsername() {
        return attendeeId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
