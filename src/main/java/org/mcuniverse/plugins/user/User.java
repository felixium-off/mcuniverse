package org.mcuniverse.plugins.user;

import java.util.Date;
import java.util.UUID;

public interface User {
    
    UUID getUuid();
    String getUsername();
    
    String getCreateIp();
    String getLastUpdateIp();
    void setLastUpdateIp(String ip);

    Date getLockedDate();
    void setLockedDate(Date date);
    boolean isLocked(); // 비즈니스 로직: 현재 시간 기준 잠김 여부 확인

    Date getRankExpiredDate();
    void setRankExpiredDate(Date date);

    Date getDateCreated();
    
    Date getLastUpdated();
    void setLastUpdated(Date date);

    boolean isEnabled();
    void setEnabled(boolean enabled);
}
