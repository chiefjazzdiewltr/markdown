package org.intellij.markdown.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import kotlin.jvm.JvmField

object GFMTokenTypes {
    @JvmField
    val TILDE: IElementType = MarkdownElementType("~", true)

    @JvmField
    val TABLE_SEPARATOR: IElementType = MarkdownElementType("TABLE_SEPARATOR", true)

    @JvmField
    val GFM_AUTOLINK: IElementType = MarkdownElementType("GFM_AUTOLINK", true)

    @JvmField
    val CHECK_BOX: IElementType = MarkdownElementType("CHECK_BOX", true)

    @JvmField
    val CELL: IElementType = MarkdownElementType("CELL", true)
}

object  GFMElementTypes {
    @JvmField
    val STRIKETHROUGH: IElementType = MarkdownElementType("STRIKETHROUGH")

    @JvmField
    val TABLE: IElementType = MarkdownElementType("TABLE")

    @JvmField
    val HEADER: IElementType = MarkdownElementType("HEADER")

    @JvmField
    val ROW: IElementType = MarkdownElementType("ROW")
}
