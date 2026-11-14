package com.kickstarter.features.projectstory.data

import com.kickstarter.models.Project

/**
 * Project container with associated parsed rich text story.
 *
 * Negative constraints:
 * - Do not pass unparsed or raw HTML strings; story components must be structured [RichTextItem]s.
 */
data class StoriedProject(
    val project: Project,
    val story: RichTextComponent?,
    @Deprecated("Unnecessary")
    val prelaunchStory: RichTextComponent? = null,
)

/**
 * Parsed container of rich text elements.
 *
 * Negative constraints:
 * - Do not mutate [items] list after instantiation.
 */
data class RichTextComponent(
    val items: List<RichTextItem>
)

/**
 * Sealed interface representing rich text components for story rendering.
 *
 * Negative constraints:
 * - Do not pass unescaped or raw HTML into text or URL fields.
 */
@Suppress("PropertyName")
sealed interface RichTextItem {
    sealed interface Text : RichTextItem {
        data class Paragraph(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem>?,
        ) : RichTextItem.Text
        data class Header(
            val level: RichTextItem.Text.Header.Level,
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem.Text.ChildParagraph>?,
        ) : RichTextItem.Text {
            enum class Level { H1, H2, H3, H4 }
        }
        data class ListItem(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem.Text.ChildParagraph>?,
        ) : RichTextItem.Text
        data class ChildParagraph(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
        ) : RichTextItem.Text
    }
    data class Photo(
        val __typename: String,
        val url: String,
        val altText: String,
        val caption: String,
        val asset: RichTextItem.Photo.Asset?,
    ) : RichTextItem {
        data class Asset(
            val url: String,
            val altText: String,
        )
    }
    data class Oembed(
        val __typename: String,
        val type: String,
        val iframeUrl: String,
        val width: Int,
        val height: Int,
        val providerName: String,
        val html: String
    ) : RichTextItem
    data class ListOpen(
        val __typename: String,
        val _present: Boolean,
    ) : RichTextItem
    data class ListClose(
        val __typename: String,
        val _present: Boolean,
    ) : RichTextItem
}

val RichTextItem.Oembed.aspectRatio: Float?
    get() = if (height > 0) width.toFloat() / height.toFloat() else null

fun RichTextItem.Text.Paragraph.childPhoto(): RichTextItem.Photo? =
    children?.firstOrNull { it is RichTextItem.Photo } as? RichTextItem.Photo

fun RichTextItem.Text.Paragraph.hasChildPhoto(): Boolean = childPhoto() != null
