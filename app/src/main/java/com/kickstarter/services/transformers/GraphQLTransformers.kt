package com.kickstarter.services.transformers

import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.FlaggingOptionsQuery
import com.kickstarter.models.AiDisclosure
import com.kickstarter.models.Category
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.Location
import com.kickstarter.models.ProjectFaq
import com.kickstarter.models.Relay
import com.kickstarter.models.Video
import com.kickstarter.type.FlaggingNodeKind
import java.nio.charset.Charset
import kotlin.math.absoluteValue

/**
 * Set of package level functions that will be used to transform the GraphQL data structures into
 * Kickstarter Data Models.
 */

fun decodeRelayId(encodedRelayId: String?): Long? {
    return try {
        encodedRelayId?.let {
            String(Base64Utils.decode(it), Charset.defaultCharset())
                .replaceBeforeLast("-", "", "")
                .toLong()
                .absoluteValue
        }
    } catch (e: Exception) {
        null
    }
}

fun <T : Relay> encodeRelayId(relay: T): String {
    val classSimpleName = relay.javaClass.simpleName.replaceFirst("AutoParcel_", "")
    val id = relay.id()
    return Base64Utils.encodeUrlSafe(("$classSimpleName-$id").toByteArray(Charset.defaultCharset()))
}

/**
 * Transform the ProjectFaq GraphQL data structure into our own ProjectFaq data model
 * @param fragment.Faq faq
 * @return ProjectFaq
 */
fun projectFaqTransformer(faq: com.kickstarter.fragment.Faq): ProjectFaq {
    val id = decodeRelayId(faq.id) ?: -1
    val answer = faq.answer
    val createdAt = faq.createdAt
    val question = faq.question

    return ProjectFaq.builder()
        .id(id)
        .answer(answer)
        .createdAt(createdAt)
        .question(question)
        .build()
}

/**
 * Transform the EnvironmentalCommitment GraphQL data structure into our own EnvironmentalCommitment data model
 * @param fragment.EnvironmentalCommitment
 * @return EnvironmentalCommitment
 */
fun environmentalCommitmentTransformer(envCommit: com.kickstarter.fragment.EnvironmentalCommitment): EnvironmentalCommitment {
    val id = decodeRelayId(envCommit.id) ?: -1
    val category = envCommit.commitmentCategory.name
    val description = envCommit.description

    return EnvironmentalCommitment.builder()
        .id(id = id)
        .category(category = category)
        .description(description = description)
        .build()
}

/**
 * Transform the AiDisclosure GraphQL data structure into our own AiDisclosure data model
 * @param FullProject.AiDisclosure aiDisclosureGraph
 * @return AiDisclosure
 */
fun aiDisclosureTransformer(aiDisclosureGraph: com.kickstarter.fragment.AiDisclosure): AiDisclosure {
    return AiDisclosure.builder()
        .id(decodeRelayId(aiDisclosureGraph.id) ?: -1)
        .fundingForAiAttribution(aiDisclosureGraph.fundingForAiAttribution)
        .fundingForAiConsent(aiDisclosureGraph.fundingForAiConsent)
        .fundingForAiOption(aiDisclosureGraph.fundingForAiOption)
        .generatedByAiConsent(aiDisclosureGraph.generatedByAiConsent)
        .generatedByAiDetails(aiDisclosureGraph.generatedByAiDetails)
        .otherAiDetails(aiDisclosureGraph.otherAiDetails)
        .build()
}

/**
 * Transform the Category GraphQL data structure into our own Categroy data model
 * @param fragment.Category category
 * @return Category
 */
fun categoryTransformer(categoryFragment: com.kickstarter.fragment.Category?): Category {
    val analyticsName = categoryFragment?.analyticsName ?: ""
    val name = categoryFragment?.name ?: ""
    val id = decodeRelayId(categoryFragment?.id) ?: -1
    val slug = categoryFragment?.slug
    val parentId = decodeRelayId(categoryFragment?.parentCategory?.id) ?: 0
    val parentName = categoryFragment?.parentCategory?.name
    val parentSlug = categoryFragment?.parentCategory?.slug
    val parentAnalyticName = categoryFragment?.parentCategory?.analyticsName ?: ""

    val parentCategory = if (parentId > 0) {
        Category.builder()
            .slug(parentSlug)
            .analyticsName(parentAnalyticName)
            .id(parentId)
            .name(parentName)
            .build()
    } else null

    return Category.builder()
        .analyticsName(analyticsName)
        .id(id)
        .name(name)
        .slug(slug)
        .parent(parentCategory)
        .parentId(parentId)
        .parentName(parentName)
        .build()
}

/**
 * Transform a single `flaggingOptions` GraphQL node into our own [FlaggingOption] data model.
 * @param node FlaggingOptionsQuery.FlaggingOption
 * @return FlaggingOption
 */
fun flaggingOptionTransformer(node: FlaggingOptionsQuery.FlaggingOption): FlaggingOption {
    return FlaggingOption(
        id = node.id,
        parentId = node.parentId,
        kind = node.kind?.rawValue,
        isGroup = node.nodeType == FlaggingNodeKind.GROUP,
        title = node.title,
        subtitle = node.subtitle,
        placeholder = node.placeholder
    )
}

/**
 * Transform the Video GraphQL data structure into our own Video data model
 * @param fragment.Video video
 * @return Video
 */
fun videoTransformer(video: com.kickstarter.fragment.Video?): Video {
    val frame = video?.previewImageUrl
    val base = video?.videoSources?.base?.src
    val high = video?.videoSources?.high?.src
    val hls = video?.videoSources?.hls?.src

    return Video.builder()
        .base(base)
        .frame(frame)
        .high(high)
        .hls(hls)
        .build()
}

/**
 * Transform the fragment.Location GraphQL data structure into our own Location data model
 * @param fragment.Location
 * @return Location
 */
fun locationTransformer(locationGR: com.kickstarter.fragment.Location?): Location {
    val id = decodeRelayId(locationGR?.id) ?: -1
    val country = locationGR?.country ?: ""
    val displayName = locationGR?.displayableName
    val name = locationGR?.name

    return Location.builder()
        .id(id)
        .country(country)
        .displayableName(displayName)
        .name(name)
        .build()
}
