package com.dd.friction.domain.model

private val WHITESPACE = Regex("\\s+")

/** Case and spacing insensitive form used for exact name and phrase matching. */
internal fun String.normalizeForMatch(): String = trim().lowercase().replace(WHITESPACE, " ")

/** The sentence that must be typed before friction can be lowered. */
object UnlockPhrase {
    const val TEXT = "I choose distraction over focus"

    fun matches(input: String): Boolean = input.normalizeForMatch() == TEXT.normalizeForMatch()
}

/** A reason to open a distraction must be a real sentence, not a single key press. */
object IntentionRule {
    const val MIN_WORDS = 3
    private const val MIN_WORD_LENGTH = 2

    fun isSatisfied(text: String): Boolean =
        text.trim().split(WHITESPACE).count { it.length >= MIN_WORD_LENGTH } >= MIN_WORDS
}
