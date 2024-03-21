package com.enaboapps.switchify.keyboard

sealed class KeyType {
    data class Character(val char: String) : KeyType() {
        override fun toString() = char
    }

    object Backspace : KeyType() {
        override fun toString() = "⌫"
    }

    object Space : KeyType() {
        override fun toString() = " "
    }

    object Return : KeyType() {
        override fun toString() = "⏎"
    }

    object Shift : KeyType() {
        override fun toString() = "⇧"
    }

    data class Special(val symbol: String) : KeyType() {
        override fun toString() = symbol
    }

    object SwitchToSymbols : KeyType() {
        override fun toString() = "?123"
    }

    object SwitchToAlphabetic : KeyType() {
        override fun toString() = "ABC"
    }
}

enum class KeyboardLayoutType {
    AlphabeticLower, AlphabeticUpper, Symbols
}

interface KeyboardLayoutListener {
    fun onLayoutChanged(layoutType: KeyboardLayoutType)
}

object KeyboardLayoutManager {
    var listener: KeyboardLayoutListener? = null

    private val alphabeticLowerLayout = listOf(
        listOf(
            KeyType.Character("q"),
            KeyType.Character("w"),
            KeyType.Character("e"),
            KeyType.Character("r"),
            KeyType.Character("t"),
            KeyType.Character("y"),
            KeyType.Character("u"),
            KeyType.Character("i"),
            KeyType.Character("o"),
            KeyType.Character("p")
        ),
        listOf(
            KeyType.Character("a"),
            KeyType.Character("s"),
            KeyType.Character("d"),
            KeyType.Character("f"),
            KeyType.Character("g"),
            KeyType.Character("h"),
            KeyType.Character("j"),
            KeyType.Character("k"),
            KeyType.Character("l")
        ),
        listOf(
            KeyType.Shift,
            KeyType.Character("z"),
            KeyType.Character("x"),
            KeyType.Character("c"),
            KeyType.Character("v"),
            KeyType.Character("b"),
            KeyType.Character("n"),
            KeyType.Character("m"),
            KeyType.Backspace
        ),
        listOf(KeyType.SwitchToSymbols, KeyType.Space, KeyType.Return)
    )

    private val alphabeticUpperLayout = alphabeticLowerLayout.map { row ->
        row.map { key ->
            when (key) {
                is KeyType.Character -> KeyType.Character(key.char.uppercase())
                else -> key
            }
        }
    }

    private val symbolsLayout = listOf(
        listOf(
            KeyType.Special("1"),
            KeyType.Special("2"),
            KeyType.Special("3"),
            KeyType.Special("4"),
            KeyType.Special("5"),
            KeyType.Special("6"),
            KeyType.Special("7"),
            KeyType.Special("8"),
            KeyType.Special("9"),
            KeyType.Special("0")
        ),
        listOf(
            KeyType.Special("@"),
            KeyType.Special("#"),
            KeyType.Special("$"),
            KeyType.Special("%"),
            KeyType.Special("&"),
            KeyType.Special("*"),
            KeyType.Special("-"),
            KeyType.Special("+"),
            KeyType.Special("("),
            KeyType.Special(")")
        ),
        listOf(
            KeyType.Shift,
            KeyType.Special("!"),
            KeyType.Special("\""),
            KeyType.Special("'"),
            KeyType.Special(":"),
            KeyType.Special(";"),
            KeyType.Special("/"),
            KeyType.Special("?"),
            KeyType.Backspace
        ),
        listOf(KeyType.SwitchToAlphabetic, KeyType.Space, KeyType.Return)
    )

    private val layouts = mapOf(
        KeyboardLayoutType.AlphabeticLower to alphabeticLowerLayout,
        KeyboardLayoutType.AlphabeticUpper to alphabeticUpperLayout,
        KeyboardLayoutType.Symbols to symbolsLayout
    )

    var currentLayoutType: KeyboardLayoutType = KeyboardLayoutType.AlphabeticLower

    val currentLayout: List<List<KeyType>>
        get() = layouts[currentLayoutType] ?: listOf()

    fun switchLayout(layoutType: KeyboardLayoutType) {
        currentLayoutType = layoutType
        listener?.onLayoutChanged(layoutType)
    }

    fun toggleShift() {
        currentLayoutType = when (currentLayoutType) {
            KeyboardLayoutType.AlphabeticLower -> KeyboardLayoutType.AlphabeticUpper
            KeyboardLayoutType.AlphabeticUpper -> KeyboardLayoutType.AlphabeticLower
            else -> return
        }
        listener?.onLayoutChanged(currentLayoutType)
    }

    // Additional functionality as needed
}