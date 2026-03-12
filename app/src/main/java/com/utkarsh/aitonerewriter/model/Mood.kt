package com.utkarsh.aitonerewriter.model

enum class Mood(
    val displayName: String,
    val emoji: String,
    val systemPrompt: String
) {
    PROFESSIONAL(
        displayName = "Professional",
        emoji = "💼",
        systemPrompt = "Rewrite the following message in a professional and formal tone. Keep the original meaning and context intact. Be concise and business-appropriate."
    ),
    FRIENDLY(
        displayName = "Friendly",
        emoji = "😊",
        systemPrompt = "Rewrite the following message in a warm, friendly, and approachable tone. Keep the original meaning and context intact. Sound natural and personable."
    ),
    FUNNY(
        displayName = "Funny",
        emoji = "😂",
        systemPrompt = "Rewrite the following message in a humorous and witty tone. Keep the original meaning and context intact. Add light humor without being offensive."
    ),
    ROMANTIC(
        displayName = "Romantic",
        emoji = "❤️",
        systemPrompt = "Rewrite the following message in a romantic and affectionate tone. Keep the original meaning and context intact. Be sweet and heartfelt."
    ),
    FLIRTY(
        displayName = "Flirty",
        emoji = "😏",
        systemPrompt = "Rewrite the following message in a playful and flirty tone. Keep the original meaning and context intact. Be charming and subtly suggestive without being inappropriate."
    ),
    POLITE(
        displayName = "Polite",
        emoji = "🙏",
        systemPrompt = "Rewrite the following message in a very polite and courteous tone. Keep the original meaning and context intact. Be respectful and considerate."
    ),
    SHORT_AND_CRISP(
        displayName = "Short & Crisp",
        emoji = "⚡",
        systemPrompt = "Rewrite the following message to be as short and concise as possible. Keep the original meaning intact. Remove all unnecessary words. Be direct."
    ),
    ASSERTIVE(
        displayName = "Assertive",
        emoji = "💪",
        systemPrompt = "Rewrite the following message in a confident and assertive tone. Keep the original meaning and context intact. Be direct, clear, and commanding without being aggressive."
    );

    companion object {
        fun fromDisplayName(name: String): Mood? {
            return entries.find { it.displayName == name }
        }
    }
}
