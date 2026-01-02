package org.mcuniverse.island;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;

import java.util.*;

public class Island {
    private final UUID ownerUuid;
    private final String islandId;
    private InstanceContainer instance;
    private Pos spawnPoint;
    private long createdAt;
    private final List<UUID> members; // 섬 멤버 리스트
    private final Map<UUID, MemberRole> memberRoles; // 멤버 역할
    
    public enum MemberRole {
        OWNER,    // 주인
        MODERATOR, // 관리자
        MEMBER    // 일반 멤버
    }
    
    public Island(UUID ownerUuid, String islandId, InstanceContainer instance, Pos spawnPoint) {
        this.ownerUuid = ownerUuid;
        this.islandId = islandId;
        this.instance = instance;
        this.spawnPoint = spawnPoint;
        this.createdAt = System.currentTimeMillis();
        this.members = new ArrayList<>();
        this.memberRoles = new HashMap<>();
        // 주인을 멤버로 추가
        this.members.add(ownerUuid);
        this.memberRoles.put(ownerUuid, MemberRole.OWNER);
    }
    
    public UUID getOwnerUuid() {
        return ownerUuid;
    }
    
    public String getIslandId() {
        return islandId;
    }
    
    public InstanceContainer getInstance() {
        return instance;
    }
    
    public Pos getSpawnPoint() {
        return spawnPoint;
    }
    
    public void setSpawnPoint(Pos spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    public MemberRole getMemberRole(UUID playerUuid) {
        return memberRoles.getOrDefault(playerUuid, null);
    }
    
    public boolean isMember(UUID playerUuid) {
        return members.contains(playerUuid);
    }
    
    public boolean isOwner(UUID playerUuid) {
        return ownerUuid.equals(playerUuid);
    }
    
    public boolean addMember(UUID playerUuid) {
        if (!members.contains(playerUuid)) {
            members.add(playerUuid);
            memberRoles.put(playerUuid, MemberRole.MEMBER);
            return true;
        }
        return false;
    }
    
    public boolean removeMember(UUID playerUuid) {
        if (members.remove(playerUuid)) {
            memberRoles.remove(playerUuid);
            return true;
        }
        return false;
    }
    
    public boolean setMemberRole(UUID playerUuid, MemberRole role) {
        if (members.contains(playerUuid) && !isOwner(playerUuid)) {
            memberRoles.put(playerUuid, role);
            return true;
        }
        return false;
    }
}