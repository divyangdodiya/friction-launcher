package com.dd.friction.domain.model

/** How the menu search matches apps, from most friction to least. */
enum class SearchMode(val frictionLevel: Int) {
    /** Only the complete app name matches, for every app. */
    Exact(frictionLevel = 2),

    /** Partial names match normal apps; distractions still need their complete name. */
    Smart(frictionLevel = 1),

    /** Partial names match every app. */
    Free(frictionLevel = 0),
    ;

    companion object {
        val DEFAULT = Smart
    }
}
