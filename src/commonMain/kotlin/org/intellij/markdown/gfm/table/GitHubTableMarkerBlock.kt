package org.intellij.markdown.gfm.table

import org.intellij.markdown.gfm.GFMElementTypes
import org.intellij.markdown.gfm.GFMTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.eatItselfFromString
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

class GitHubTableMarkerBlock(pos: LookaheadText.Position,
                             constraints: MarkdownConstraints,
                             private val productionHolder: ProductionHolder,
                             private val tableColumnsNumber: Int)
: MarkerBlockImpl(constraints, productionHolder.mark()) {

    var currentLine = 0

    init {
        productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset..pos.nextLineOrEofOffset,
                GFMElementTypes.HEADER)))
        productionHolder.addProduction(fillCells(pos))
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        currentLine++
        // That means it's table header separator line
        if (currentLine == 1) {
            productionHolder.addProduction(listOf(SequentialParser.Node(pos.offset + 1..pos.nextLineOrEofOffset,
                    GFMTokenTypes.TABLE_SEPARATOR)))
            return MarkerBlock.ProcessingResult.CANCEL
        }

        val line = pos.currentLine
        if (!isProbablyTableLine(line)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }
        val cellsAndSeps = fillCells(pos)
        if (cellsAndSeps.isEmpty()) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }
        productionHolder.addProduction(
                listOf(SequentialParser.Node(cellsAndSeps.first().range.first..cellsAndSeps.last().range.last,
                        GFMElementTypes.ROW))
                        + cellsAndSeps)
        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultAction() = MarkerBlock.ClosingAction.DONE

    override fun calcNextInterestingOffset(pos: LookaheadText.Position) = pos.nextLineOrEofOffset

    override fun getDefaultNodeType() = GFMElementTypes.TABLE

    override fun isInterestingOffset(pos: LookaheadText.Position) = pos.offsetInCurrentLine == -1

    override fun allowsSubBlocks() = false

    private fun fillCells(pos: LookaheadText.Position): List<SequentialParser.Node> {
        val result = ArrayList<SequentialParser.Node>()

        var offset = pos.offset
        if (pos.offsetInCurrentLine == -1) {
            offset += 1 + constraints.getCharsEaten(pos.currentLine)
        }

        val line = constraints.eatItselfFromString(pos.currentLine)

        val cells = splitByPipes(line)
        var cellNodesAdded = 0
        for (i in cells.indices) {
            val cell = cells[i]
            if (!cell.isBlank() || i in 1..cells.lastIndex - 1) {
                result.add(SequentialParser.Node(offset..offset + cell.length, GFMTokenTypes.CELL))
                cellNodesAdded++
            }
            offset += cell.length
            if (i < cells.lastIndex) {
                result.add(SequentialParser.Node(offset..offset + 1, GFMTokenTypes.TABLE_SEPARATOR))
            }
            offset += 1

            if (cellNodesAdded >= tableColumnsNumber) {
                if (offset < pos.nextLineOrEofOffset) {
                    result.add(SequentialParser.Node(offset..pos.nextLineOrEofOffset, GFMTokenTypes.TABLE_SEPARATOR))
                }
                break
            }
        }
        return result
    }

    private fun isProbablyTableLine(line: CharSequence): Boolean {
        return line.contains('|')
    }

    companion object {
        fun splitByPipes(text: CharSequence): List<String> {
            val result = arrayListOf<String>()
            var startIndex = 0
            for (index in text.indices) {
                val char = text[index]
                if (char != '|') {
                    continue
                }
                val charBefore = text[(index - 1).coerceAtLeast(0)]
                if (charBefore == '\\') {
                    continue
                }
                val substring = text.substring(startIndex, index)
                result.add(substring)
                startIndex = index + 1
            }
            result.add(text.substring(startIndex))
            return result
        }
    }
}
