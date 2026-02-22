package org.mcuniverse.plugins.user;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserImpl implements User {

    // Immutable Fields (생성 후 변경 불가)
    @Setter(AccessLevel.NONE) // Setter 생성 방지
    private final UUID uuid;
    
    @Setter(AccessLevel.NONE)
    private final String username;
    
    @Setter(AccessLevel.NONE)
    private final String createIp;
    
    @Setter(AccessLevel.NONE)
    @Builder.Default
    private final Date dateCreated = new Date();

    // Mutable Fields (변경 가능)
    private String lastUpdateIp;
    private Date locked;
    private Date rankExpired;
    
    @Builder.Default
    private Date lastUpdated = new Date();
    
    @Builder.Default
    private boolean enabled = true;

    @Override
    public Date getLockedDate() {
        return locked;
    }

    @Override
    public void setLockedDate(Date date) {
        this.locked = date;
    }

    @Override
    public boolean isLocked() {
        return locked != null;
    }

    @Override
    public Date getRankExpiredDate() {
        return rankExpired;
    }

    @Override
    public void setRankExpiredDate(Date rankExpired) {
        this.rankExpired = rankExpired;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}