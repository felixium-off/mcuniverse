package org.mcuniverse.plugins.rank;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class RankParameterType implements ParameterType {
    @Override
    public Object parse(@NotNull MutableStringStream input, @NotNull ExecutionContext context) {
        String rankId = input.readString();

        if (RankGroup.REGISTRY.containsKey(rankId)) return RankGroup.REGISTRY.get(rankId);

        return new RankGroup.RankImpl(rankId, rankId, Integer.MAX_VALUE);
    }

    @Override
    public SuggestionProvider<MinestomCommandActor> defaultSuggestions() {
        return SuggestionProvider.of(RankGroup.REGISTRY.keySet().toArray(new String[0]));
    }
}
