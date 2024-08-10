package bob.e2e.dto

data class KeypadResponse(
    val keypadImage: String,
    val hasList: List<String>,
    val sessionId: String
)