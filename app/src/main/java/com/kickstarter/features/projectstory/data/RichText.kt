package com.kickstarter.features.projectstory.data

import com.kickstarter.models.Project

/**
 * Project container with associated parsed rich text story.
 *
 * Negative constraints:
 * - Do not pass unparsed or raw HTML strings; story components must be structured [RichTextItem]s.
 *
 * @property project Associated project domain model.
 * @property story Parsed rich text component tree for the story campaign.
 * @property prelaunchStory Optional pre-launch story component tree.
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
 *
 * @property items List of rich text items making up the project campaign story.
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
    /**
     * Textual story element.
     *
     * Negative constraints:
     * - Do not pass unescaped HTML entities.
     */
    sealed interface Text : RichTextItem {
        /**
         * Story paragraph block.
         *
         * Negative constraints:
         * - Do not pass raw HTML.
         */
        data class Paragraph(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem>?,
        ) : RichTextItem.Text

        /**
         * Story heading element.
         *
         * Negative constraints:
         * - Do not pass invalid heading level.
         */
        data class Header(
            val level: RichTextItem.Text.Header.Level,
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem.Text.ChildParagraph>?,
        ) : RichTextItem.Text {
            /**
             * Heading level enumeration (H1 to H4).
             *
             * Negative constraints:
             * - Do not use for non-heading elements.
             */
            enum class Level { H1, H2, H3, H4 }
        }

        /**
         * Bulleted or numbered list item.
         *
         * Negative constraints:
         * - Do not render without enclosing list container.
         */
        data class ListItem(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem.Text.ChildParagraph>?,
        ) : RichTextItem.Text

        /**
         * Child paragraph span within a block.
         *
         * Negative constraints:
         * - Do not pass unescaped text.
         */
        data class ChildParagraph(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
        ) : RichTextItem.Text
    }

    /**
     * Embedded image photo item.
     *
     * Negative constraints:
     * - Do not pass blank image URLs.
     */
    data class Photo(
        val __typename: String,
        val url: String,
        val altText: String,
        val caption: String,
        val asset: RichTextItem.Photo.Asset?,
    ) : RichTextItem {
        /**
         * Underlying image asset metadata.
         *
         * Negative constraints:
         * - Do not construct with blank URLs.
         */
        data class Asset(
            val url: String,
            val altText: String,
        )
    }

    /**
     * Embedded rich media (video, audio, social embed).
     *
     * Negative constraints:
     * - Do not pass non-HTTPS embed iframe URLs.
     */
    data class Oembed(
        val __typename: String,
        val type: String,
        val iframeUrl: String,
        val width: Int,
        val height: Int,
        val providerName: String,
        val html: String
    ) : RichTextItem

    /**
     * Delimiter opening a list.
     *
     * Negative constraints:
     * - Do not use as standalone content item.
     */
    data class ListOpen(
        val __typename: String,
        val _present: Boolean,
    ) : RichTextItem

    /**
     * Delimiter closing a list.
     *
     * Negative constraints:
     * - Do not use as standalone content item.
     */
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
