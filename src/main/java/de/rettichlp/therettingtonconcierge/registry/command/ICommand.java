package de.rettichlp.therettingtonconcierge.registry.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jspecify.annotations.NonNull;

public interface ICommand<T> {

    @NonNull LiteralArgumentBuilder<@NonNull T> node(@NonNull LiteralArgumentBuilder<@NonNull T> commandBuilder);
}
