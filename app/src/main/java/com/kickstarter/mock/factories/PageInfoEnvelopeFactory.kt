package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

/**
 * Test mock implementation for PageInfoEnvelopeFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
class PageInfoEnvelopeFactory {

    companion object {
        fun pageInfoEnvelope(): PageInfoEnvelope {
            return PageInfoEnvelope.builder()
                .endCursor("WzMyNDk1MzMzXQ==")
                .startCursor("WzMyNDk1MzMzXQ==")
                .hasPreviousPage(false)
                .hasNextPage(false)
                .build()
        }

        fun pageInfoEnvelopeHasPrevious(): PageInfoEnvelope {
            return PageInfoEnvelope.builder()
                .endCursor("WzMyNDk1MzMzXQ==")
                .startCursor("WzMyNDk1MzMzXQ==")
                .hasPreviousPage(true)
                .hasNextPage(false)
                .build()
        }

        fun pageInfoEnvelopeHasNext(): PageInfoEnvelope {
            return PageInfoEnvelope.builder()
                .endCursor("WzMyNDk1MzMzXQ==")
                .startCursor("WzMyNDk1MzMzXQ==")
                .hasPreviousPage(false)
                .hasNextPage(true)
                .build()
        }
    }
}
