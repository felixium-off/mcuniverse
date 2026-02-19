package org.mcuniverse.plugins.economy;

import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class CurrencyParameterType implements ParameterType<MinestomCommandActor, Currency> {

    @Override
    public Currency parse(MutableStringStream input, ExecutionContext<MinestomCommandActor> context) {
        String currencyId = input.readString();

        if (currencyId.equalsIgnoreCase("balance")) return Currency.BALANCE;
        if (currencyId.equalsIgnoreCase("cash")) return Currency.CASH;

        return new Currency.CurrencyImpl(currencyId, currencyId);
    }

    // (4) [선택] 탭 자동완성 제어 (입력 추천 목록)
    @Override
    public SuggestionProvider<MinestomCommandActor> defaultSuggestions() {
        return SuggestionProvider.of("balance", "cash");
    }
}