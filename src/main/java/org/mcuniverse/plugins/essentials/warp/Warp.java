package org.mcuniverse.plugins.essentials.warp;

import net.minestom.server.coordinate.Pos;

/**
 * 워프 정보를 담는 불변 객체(Record)입니다.
 * @param name 워프 이름
 * @param position 좌표 (x, y, z, yaw, pitch)
 * @param instanceName 월드(인스턴스) 이름 (멀티월드 지원 대비)
 */
public record Warp(
    String name,
    Pos position,
    String instanceName
) {
}