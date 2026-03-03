package de.rettichlp.therettingtonconcierge.registry.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.atteo.classindex.IndexSubclasses;
import org.jspecify.annotations.NonNull;

@IndexSubclasses
public interface ICommand {

    /**
     * Enhances a given {@link LiteralArgumentBuilder} with additional configurations or modifications. This method is used to
     * construct or augment command node structures for a command system.
     *
     * @param node the {@link LiteralArgumentBuilder} instance representing the initial command node. This parameter must not be null.
     *
     * @return the modified {@link LiteralArgumentBuilder} instance with applied enhancements. This return value is never null.
     */
    @NonNull LiteralArgumentBuilder<@NonNull CommandSourceStack> node(@NonNull LiteralArgumentBuilder<@NonNull CommandSourceStack> node);
}
