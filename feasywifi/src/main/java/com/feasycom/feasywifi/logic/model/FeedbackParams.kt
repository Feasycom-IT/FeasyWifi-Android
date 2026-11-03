package com.feasycom.feasywifi.logic.model

data class FeedbackParams(
    val markdown: Markdown, val msgtype: String = "markdown"
) {
    data class Markdown(
        val content: String
    )

    data class Params(val content: String, val feedbackType: Int)

}

