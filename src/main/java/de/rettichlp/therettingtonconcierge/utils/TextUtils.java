package de.rettichlp.therettingtonconcierge.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.stream;
import static net.kyori.adventure.translation.GlobalTranslator.render;

public class TextUtils {

    /**
     * Converts a translation key and optional arguments into a localized {@link Component} for display, while taking into account the
     * specified locale.
     *
     * @param translationKey The translation key representing the text to be localized. Must not be null.
     * @param locale         The locale for which the text should be localized. Must not be null.
     * @param args           Optional arguments to be interpolated into the localized text. Can be empty.
     *
     * @return A non-null {@link Component} containing the localized text with any provided arguments.
     */
    public static @NonNull Component translationAsComponent(@NonNull String translationKey, @NonNull Locale locale, Object... args) {
        List<@NotNull TextComponent> argComponents = stream(args).map(Object::toString).map(Component::text).toList();
        TranslatableComponent translatable = Component.translatable(translationKey, argComponents);
        return render(translatable, locale);
    }

    /**
     * Splits the given text into a list of lines, where each line is limited to the specified maximum length. Lines are constructed by
     * preserving words to ensure they do not exceed the maximum length. If the input contains newline characters, paragraphs are split
     * accordingly and processed individually.
     *
     * @param text   The input text to be split into lines. Must not be null.
     * @param length The maximum length of each line. Must be a positive integer.
     *
     * @return A list of strings representing the split lines, with no line exceeding the specified length.
     */
    public static @NonNull List<String> splitIntoLines(@NonNull String text, int length) {
        List<String> lines = new ArrayList<>();

        // check for line breaks
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");

            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > length) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                }

                currentLine.append(word).append(" ");
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString().trim());
            }
        }

        return lines;
    }

    /**
     * Converts an integer to its corresponding Roman numeral representation. Roman numerals are defined for integers in the range of 1
     * to 10. For any number outside this range, the method returns the string representation of the input integer.
     *
     * @param i The integer value to be converted to a Roman numeral. Must be a non-negative integer.
     *
     * @return A string representing the Roman numeral if the integer is within the range 1 to 10, otherwise the string representation
     *         of the input integer.
     */
    @Contract(pure = true)
    public static @NonNull String integerToRomanString(int i) {
        return switch (i) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(i);
        };
    }
}
