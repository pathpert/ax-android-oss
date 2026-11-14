package com.kickstarter.libs

import android.os.Parcelable
import com.kickstarter.services.DiscoveryParams
import kotlinx.parcelize.Parcelize

/**
 * A [RefTag] is a string identifier that Kickstarter uses to credit a pledge to a source of traffic, e.g. discovery,
 * activity, search, etc. This class represents all possible ref tags we support in the app.
 */

@Parcelize
class RefTag private constructor(
    private val tag: String
) : Parcelable {
    fun tag() = this.tag

    @Parcelize
    data class Builder(
        private var tag: String = ""
    ) : Parcelable {
        fun tag(tag: String) = apply { this.tag = tag }
        fun build() = RefTag(
            tag = tag
        )
    }
    fun toBuilder() = Builder(
        tag = tag
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()

        @JvmStatic
        fun from(tag: String): RefTag {
            return builder().tag(tag).build()
        }

        /** RefTag for project navigation from activity feed. */
        fun activity(): RefTag {
            return builder().tag("activity").build()
        }

        /** RefTag for project navigation from discovery activity sample. */
        fun activitySample(): RefTag {
            return builder().tag("discovery_activity_sample").build()
        }

        /** RefTag for project navigation from category discovery. */
        @JvmStatic
        fun category(): RefTag {
            return builder().tag("category").build()
        }

        /** RefTag for project navigation from sorted category discovery. */
        @JvmStatic
        fun category(sort: DiscoveryParams.Sort): RefTag {
            return builder().tag("category" + sort.refTagSuffix()).build()
        }

        /** RefTag for project navigation from featured category hero card. */
        @JvmStatic
        fun categoryFeatured(): RefTag {
            return builder().tag("category_featured").build()
        }

        /** RefTag for project navigation from city/location discovery. */
        fun city(): RefTag {
            return builder().tag("city").build()
        }

        /** RefTag for project navigation from themed project collection. */
        @JvmStatic
        fun collection(tagId: Int): RefTag {
            return builder().tag("android_project_collection_tag_$tagId").build()
        }

        /** RefTag for project navigation from creator dashboard. */
        @JvmStatic
        fun dashboard(): RefTag {
            return builder().tag("dashboard").build()
        }

        /** RefTag for project navigation via incoming Android deep link. */
        fun deepLink(): RefTag {
            return builder().tag("android_deep_link").build()
        }

        /** RefTag for project navigation from default discovery feed. */
        @JvmStatic
        fun discovery(): RefTag {
            return builder().tag("discovery").build()
        }

        /** RefTag for project navigation from pledge info. */
        fun pledgeInfo(): RefTag {
            return builder().tag("pledge_info").build()
        }

        /** RefTag for project navigation from in-app social share action. */
        fun projectShare(): RefTag {
            return builder().tag("android_project_share").build()
        }

        fun projectShareCopyLink(): RefTag = builder().tag("android_project_share_copy_link").build()
        fun projectShareInstagramFeed(): RefTag = builder().tag("android_project_share_instagram_feed").build()
        fun projectShareInstagramStories(): RefTag = builder().tag("android_project_share_instagram_stories").build()
        fun projectShareX(): RefTag = builder().tag("android_project_share_x").build()
        fun projectShareFacebookFeed(): RefTag = builder().tag("android_project_share_facebook_feed").build()
        fun projectShareFacebookStories(): RefTag = builder().tag("android_project_share_facebook_stories").build()
        fun projectShareWhatsApp(): RefTag = builder().tag("android_project_share_whatsapp").build()
        fun projectShareMessages(): RefTag = builder().tag("android_project_share_messages").build()
        fun projectShareEmail(): RefTag = builder().tag("android_project_share_email").build()
        fun projectShareMore(): RefTag = builder().tag("android_project_share_more").build()

        @JvmStatic
        fun push(): RefTag {
            return builder().tag("push").build()
        }

        @JvmStatic
        fun recommended(): RefTag {
            return builder().tag("recommended").build()
        }

        @JvmStatic
        fun recommended(sort: DiscoveryParams.Sort): RefTag {
            return builder().tag("recommended" + sort.refTagSuffix()).build()
        }

        @JvmStatic
        fun search(): RefTag {
            return builder().tag("search").build()
        }

        @JvmStatic
        fun searchFeatured(): RefTag {
            return builder().tag("search_featured").build()
        }

        @JvmStatic
        fun searchPopular(): RefTag {
            return builder().tag("search_popular_title_view").build()
        }

        @JvmStatic
        fun searchPopularFeatured(): RefTag {
            return builder().tag("search_popular_featured").build()
        }

        fun similarProjects(): RefTag {
            return builder().tag("similar_projects").build()
        }

        fun videoFeed(): RefTag {
            return builder().tag("video_feed").build()
        }

        fun social(): RefTag {
            return builder().tag("social").build()
        }

        fun survey(): RefTag {
            return builder().tag("survey").build()
        }

        @JvmStatic
        fun thanks(): RefTag {
            return builder().tag("thanks").build()
        }

        @JvmStatic
        fun thanksFacebookShare(): RefTag {
            return builder().tag("android_thanks_facebook_share").build()
        }

        @JvmStatic
        fun thanksTwitterShare(): RefTag {
            return builder().tag("android_thanks_twitter_share").build()
        }

        @JvmStatic
        fun thanksShare(): RefTag {
            return builder().tag("android_thanks_share").build()
        }

        @JvmStatic
        fun update(): RefTag {
            return builder().tag("update").build()
        }

        fun updateShare(): RefTag {
            return builder().tag("android_update_share").build()
        }
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is RefTag) {
            equals = tag() == obj.tag()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
