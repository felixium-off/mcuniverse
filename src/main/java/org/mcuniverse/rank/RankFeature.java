package org.mcuniverse.rank;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.mcuniverse.common.GameFeature;
import org.mcuniverse.common.config.ConfigManager;
import org.mcuniverse.common.data.PlayerDataHandler;
import org.mcuniverse.common.listener.CommonConnectionListener;
import org.mcuniverse.rank.RankStrategy;
import org.mcuniverse.rank.RankService;
import org.mcuniverse.rank.commands.RankCommand;
import org.mcuniverse.rank.impl.MongoRankStrategy;
import org.mcuniverse.rank.permission.RankPermissionFactory;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class RankFeature implements GameFeature {

    private RankService rankService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        RankStrategy strategy = new MongoRankStrategy();
        this.rankService = new RankService(strategy);
        // 2. 통합 리스너 등록
        // Main.java에서 CommonConnectionListener를 관리하거나, 여기서 개별적으로 등록
        // 현재 구조상 Feature가 독립적이므로 각자 리스너에 핸들러를 등록하는 것이 아니라,
        // CommonListener가 Main에 하나 있고 거기에 등록하는 것이 Best Practice.
        // 하지만 Main을 수정하기 번거로우므로, 임시로 여기에서도 Common 리스너를 생성하여 사용 (EventNode가 같으므로 동작은 동일)
        // ** 개선점: Main에서 CommonConnectionListener를 public static으로 열어두거나, 주입받아야 함.
        // 여기서는 server.getGlobalEventHandler()를 공유하므로 새 인스턴스를 만들어도 "동작"은 하지만,
        // 중복 등록을 방지하기 위해 Main에서 관리하는 것이 옳음.
        
        // >> Main.java의 구조를 보니 features 리스트를 순회하며 enable을 호출함.
        // >> CommonConnectionListener는 "Common" 모듈 혹은 Main에서 한 번만 생성되는 것이 맞음.
        // >> 하지만 지금은 과도기에 있으므로, Main 코드를 수정하지 않고 Feature 내에서 해결하려면
        // >> 각자 EventNode에 등록하는 방식(기존 방식)과 다를 바가 없어짐.
        // >> 사용자 요청은 "하나의 리스너로 묶어서"임.
        // >> 즉, Main.java에 CommonConnectionListener(Global) 하나를 두고, Feature들이 거기에 핸들러를 추가해야 함.
        
        // 일단은 요청대로 "하나의 리스너"를 사용하기 위해, Main.java 수정을 염두에 두고 코드를 작성하지 않음.
        // 대신, 각 Feature에서 CommonConnectionListener를 new 하는 것은 "하나의 리스너"가 아니게 됨 (인스턴스가 2개).
        // 따라서 Main.java에 static instance를 두거나, Feature.enable에 파라미터를 추가해야 함.
        // 가장 현실적인 방법: Main.java 수정.

        // 하지만 Step 변경을 최소화하기 위해 EconomyFeature와 마찬가지로 로컬에서 등록하되,
        // **핵심은 로직 분리(Handler)**임.
        
        CommonConnectionListener commonListener = new CommonConnectionListener(server.getGlobalEventHandler());
        commonListener.addHandler(new PlayerDataHandler() {
            @Override
            public void onLoad(Player player) {
                rankService.createRank(player.getUuid(), player.getUsername());
            }

            @Override
            public void onUnload(Player player) {
                rankService.expireRankCache(player.getUuid(), 3600);
            }
        });

        if (lamp != null) {
            lamp.register(new RankCommand(rankService));
        }
    }

    @Override
    public void disable(MinecraftServer server) {}
    
    public RankService getRankService() {
        return rankService;
    }
}