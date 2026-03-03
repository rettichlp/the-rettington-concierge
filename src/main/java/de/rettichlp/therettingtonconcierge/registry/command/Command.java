package de.rettichlp.therettingtonconcierge.registry.command;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to define metadata for a command within the system. This information is utilized for registering and managing
 * commands dynamically at runtime, specifically in the context of the Brigadier command system used in modern Minecraft server
 * implementations.
 * <p>
 * Commands annotated with this are expected to provide key properties such as a label (primary identifier), optional aliases,
 * permissions required to execute the command, and a disabled state.
 * <p>
 * This annotation is intended to be applied at the type level, marking classes that implement command logic.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Command {

    /**
     * Retrieves the primary label of the annotated command. The label serves as the main identifier for the command when it is
     * registered and invoked within the system.
     *
     * @return a {@link String} representing the primary name of the command.
     */
    String label();

    /**
     * Specifies an optional array of alternative names for the command. These aliases can be used as additional identifiers to invoke
     * the command.
     *
     * @return an array of {@link String} representing the command's aliases. Defaults to an empty array if no aliases are specified.
     */
    String[] aliases() default {};

    /**
     * Indicates whether the annotated command is disabled. When set to true, the command will not be registered or available for use
     * within the system.
     *
     * @return {@code true} if the command is disabled, otherwise {@code false}.
     */
    boolean disabled() default false;
}
