package org.mcuniverse.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/// 내/외부적 문자열 도구를 제공하는 유틸리티 클래스입니다.
///
/// @author Q. T. Felix
/// @since 1.0-SNAPSHOT
public final class StringUtil {

    /// 주어진 문자열을 [MiniMessage]로 역직렬화하여 [Component]로 반환합니다.
    ///
    /// @return 결과 컴포넌트
    public static Component format(final @NotNull String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
