package org.intellij.markdown.gfm

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints

class GFMConstraints(indents: IntArray,
                            types: CharArray,
                            isExplicit: BooleanArray,
                            charsEaten: Int,
                            private val isCheckbox: Boolean) : CommonMarkdownConstraints(indents, types, isExplicit, charsEaten) {
    override val base: CommonMarkdownConstraints
        get() = BASE

    override fun createNewConstraints(indents: IntArray, types: CharArray, isExplicit: BooleanArray, charsEaten: Int): CommonMarkdownConstraints {
        val initialType = types[types.size - 1]
        val originalType = toOriginalType(initialType)
        types[types.size - 1] = originalType
        return GFMConstraints(indents, types, isExplicit, charsEaten, initialType != originalType)
    }

    fun hasCheckbox(): Boolean {
        return isCheckbox
    }

    override fun fetchListMarker(pos: LookaheadText.Position): CommonMarkdownConstraints.ListMarkerInfo? {
        val baseMarkerInfo = super.fetchListMarker(pos)
                ?: return null

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine + baseMarkerInfo.markerLength

        while (offset < line.length && (line[offset] == ' ' || line[offset] == '\t')) {
            offset++
        }

        if (offset + 3 <= line.length
                && line[offset] == '['
                && line[offset + 2] == ']'
                && (line[offset + 1] == 'x' || line[offset + 1] == 'X' || line[offset + 1] == ' ')) {
            return CommonMarkdownConstraints.ListMarkerInfo(offset + 3 - pos.offsetInCurrentLine,
                    toCheckboxType(baseMarkerInfo.markerType),
                    baseMarkerInfo.markerLength)
        } else {
            return baseMarkerInfo
        }
    }

    companion object {
        val BASE: GFMConstraints = GFMConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0, false)

        private fun toCheckboxType(originalType: Char): Char {
            return (originalType.toInt() + 100).toChar()
        }

        private fun toOriginalType(checkboxType: Char): Char {
            if (checkboxType.toInt() < 128) {
                return checkboxType
            }
            return (checkboxType.toInt() - 100).toChar()
        }
    }
}