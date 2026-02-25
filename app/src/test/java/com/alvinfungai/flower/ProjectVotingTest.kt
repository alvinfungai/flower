package com.alvinfungai.flower

import junit.framework.TestCase.assertEquals
import org.junit.Test


class ProjectVotingTest {
    // helper for simulating diff login in viewmodel
    private fun calculateVoteDiff(currentVote: Boolean?, isUpvote: Boolean): Int {
        return when (currentVote) {
            null -> if (isUpvote) 1 else -1
            isUpvote -> if (isUpvote) -1 else 1
            else -> if (isUpvote) 2 else -2 // flipped vote e.g upvote <-> downvote
        }
    }

    @Test
    fun `new upvote increases score by 1`() {
        assertEquals(1, calculateVoteDiff(null, true))
    }

    @Test
    fun `new downvote decreases score by 1`() {
        assertEquals(-1, calculateVoteDiff(null, false))
    }

    @Test
    fun `switching from downvote to upvote increases score by 2`() {
        assertEquals(2, calculateVoteDiff(false, true))
    }

    @Test
    fun `switching from upvote to downvote decreases score by 2`() {
        assertEquals(-2, calculateVoteDiff(true, false))
    }
}