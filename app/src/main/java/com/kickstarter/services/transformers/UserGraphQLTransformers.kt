package com.kickstarter.services.transformers

import com.kickstarter.UserPrivacyQuery
import com.kickstarter.models.Avatar
import com.kickstarter.models.Comment
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.type.CurrencyCode
import com.kickstarter.type.Feature

/**
 * Transform the User GraphQL data structure into our own User data model
 * @param fragment.User user
 * @return User
 */
fun userTransformer(user: com.kickstarter.fragment.User?): User {
    val id = decodeRelayId(user?.id) ?: -1
    val name = user?.name
    val avatar = Avatar.builder()
        .medium(user?.imageUrl)
        .build()
    val chosenCurrency = user?.chosenCurrency ?: CurrencyCode.USD.rawValue

    return User.builder()
        .id(id)
        .name(name)
        .avatar(avatar)
        .chosenCurrency(chosenCurrency)
        .build()
}

/**
 * Transform the UserPrivacy GraphQL data structure into our own UserPrivacy data model
 * @param UserPrivacyQuery.Me userPrivacy
 * @return UserPrivacy
 */
fun userPrivacyTransformer(userPrivacy: UserPrivacyQuery.Me): UserPrivacy {
    val defaultCurrency = CurrencyCode.USD.rawValue
    return UserPrivacy(
        name = userPrivacy.name,
        email = userPrivacy.email ?: "",
        hasPassword = userPrivacy.hasPassword ?: false,
        isCreator = userPrivacy.isCreator ?: false,
        isDeliverable = userPrivacy.isDeliverable ?: false,
        isEmailVerified = userPrivacy.isEmailVerified ?: false,
        chosenCurrency = userPrivacy.chosenCurrency ?: defaultCurrency,
        enabledFeatures = userPrivacy.enabledFeatures.map { feature: Feature ->
            feature.rawValue
        }
    )
}

fun updateTransformer(post: com.kickstarter.fragment.Post?): Update {
    val id = decodeRelayId(post?.id) ?: -1
    val author = User.builder()
        .id(decodeRelayId(post?.author?.user?.id) ?: -1)
        .name(post?.author?.user?.name ?: "")
        .avatar(
            Avatar.builder()
                .medium(post?.author?.user?.imageUrl)
                .build()
        )
        .build()

    val projectId = decodeRelayId(post?.project?.id) ?: -1

    val title = post?.title ?: ""

    val publishedAt = post?.publishedAt
    val updatedAt = post?.updatedAt
    val sequence = post?.number ?: 0

    val url = post?.project?.url
    val urlsWeb = Update.Urls.Web.builder()
        .update("$url/posts/$id")
        .build()
    val updateUrl = Update.Urls.builder().web(urlsWeb).build()

    val updateFreeformPost = post?.updateFreeformPost
    val commentsCount = updateFreeformPost?.commentsCount
    val body = updateFreeformPost?.body as? String

    return Update.builder()
        .body(body)
        .commentsCount(commentsCount)
        .hasLiked(post?.isLiked)
        .id(id)
        .isPublic(post?.isPublic)
        .likesCount(post?.likesCount)
        .projectId(projectId)
        .publishedAt(publishedAt)
        .sequence(sequence)
        .title(title)
        .updatedAt(updatedAt)
        .urls(updateUrl)
        .user(author)
        .visible(post?.isVisible)
        .build()
}

fun commentTransformer(commentFr: com.kickstarter.fragment.Comment?): Comment {
    val badges: List<String> = commentFr?.authorBadges?.map { badge ->
        badge?.rawValue ?: ""
    } ?: emptyList()

    val author = User.builder()
        .id(decodeRelayId(commentFr?.author?.user?.id) ?: -1)
        .name(commentFr?.author?.user?.name ?: "")
        .avatar(
            Avatar.builder()
                .medium(commentFr?.author?.user?.imageUrl)
                .build()
        )
        .build()
    val id = decodeRelayId(commentFr?.id) ?: -1
    val repliesCount = commentFr?.replies?.totalCount ?: 0
    val body = commentFr?.body ?: ""
    val createdAt = commentFr?.createdAt
    val deleted = commentFr?.deleted ?: false
    val hasFlaggings = commentFr?.hasFlaggings ?: false
    val sustained = commentFr?.sustained ?: false
    val authorCanceled = commentFr?.authorCanceledPledge ?: false
    val parentId = decodeRelayId(commentFr?.parentId)

    return Comment.builder()
        .id(id)
        .author(author)
        .repliesCount(repliesCount)
        .body(body)
        .authorBadges(badges)
        .cursor("")
        .createdAt(createdAt)
        .deleted(deleted)
        .hasFlaggings(hasFlaggings)
        .sustained(sustained)
        .authorCanceledPledge(authorCanceled)
        .parentId(parentId)
        .build()
}
