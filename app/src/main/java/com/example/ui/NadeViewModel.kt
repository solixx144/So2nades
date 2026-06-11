package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.CustomNadeEntity
import com.example.data.FeedbackEntity
import com.example.data.DefaultNade
import com.example.data.DefaultNades
import com.example.data.GeminiApiService
import com.example.data.GeminiContent
import com.example.data.GeminiPart
import com.example.data.GeminiRequest
import com.example.data.NadeDatabase
import com.example.data.NadeRepository
import com.example.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// UI representation of a Lineup (combining default and custom entities)
data class NadeUiItem(
    val key: String, // "default_id" or "custom_id"
    val id: String,
    val map: String,
    val type: String,
    val side: String,
    val title: String,
    val site: String,
    val difficulty: String,
    val throwType: String,
    val standingSpot: String,
    val aimSpot: String,
    val description: String,
    val videoUrl: String? = null,
    val imageUrl: String? = null,
    val isCustom: Boolean,
    val isFavorite: Boolean
)

// Playbook Strat item
data class PlaybookItem(
    val nadeKey: String,
    val title: String,
    val map: String,
    val type: String,
    val isReady: Boolean = false
)

data class CombinedFilters(
    val map: String,
    val type: String,
    val side: String,
    val query: String
)

class NadeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NadeDatabase.getDatabase(application)
    private val repository = NadeRepository(db)

    // Filter flows
    val selectedMap = MutableStateFlow("All")
    val selectedType = MutableStateFlow("All")
    val selectedSide = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")
    val isAdminMode = MutableStateFlow(false)
    val selectedLanguage = MutableStateFlow("English")

    private val filtersFlow = combine(
        selectedMap,
        selectedType,
        selectedSide,
        searchQuery
    ) { mapFilter, typeFilter, sideFilter, query ->
        CombinedFilters(mapFilter, typeFilter, sideFilter, query)
    }

    // Raw Room flows
    private val customNades = repository.customNades
    private val favorites = repository.favorites

    // Feedback flow
    val feedbackList: StateFlow<List<FeedbackEntity>> = repository.feedbacks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined UI lists
    val nadesUiList: StateFlow<List<NadeUiItem>> = combine(
        customNades,
        favorites,
        filtersFlow
    ) { customs, favs, filters ->
        val mapFilter = filters.map
        val typeFilter = filters.type
        val sideFilter = filters.side
        val query = filters.query

        val favKeys = favs.map { it.nadeKey }.toSet()

        // 1. Map defaults
        val defaultUi = DefaultNades.items.map {
            val key = "default_${it.id}"
            NadeUiItem(
                key = key,
                id = it.id,
                map = it.map,
                type = it.type,
                side = it.side,
                title = it.title,
                site = it.site,
                difficulty = it.difficulty,
                throwType = it.throwType,
                standingSpot = it.standingSpot,
                aimSpot = it.aimSpot,
                description = it.description,
                videoUrl = it.videoUrl,
                imageUrl = it.imageUrl,
                isCustom = false,
                isFavorite = favKeys.contains(key)
            )
        }

        // 2. Map customs
        val customUi = customs.map {
            val key = "custom_${it.id}"
            NadeUiItem(
                key = key,
                id = it.id.toString(),
                map = it.map,
                type = it.type,
                side = it.side,
                title = it.title,
                site = "Custom Site",
                difficulty = "User-Created",
                throwType = it.throwType,
                standingSpot = it.standingSpot,
                aimSpot = it.aimSpot,
                description = it.description,
                videoUrl = it.videoUrl,
                imageUrl = it.imageUrl,
                isCustom = true,
                isFavorite = favKeys.contains(key)
            )
        }

        // Combine
        val merged = defaultUi + customUi

        // Apply filters
        merged.filter {
            (mapFilter == "All" || it.map.equals(mapFilter, ignoreCase = true)) &&
            (typeFilter == "All" || it.type.equals(typeFilter, ignoreCase = true)) &&
            (sideFilter == "All" || it.side.equals(sideFilter, ignoreCase = true)) &&
            (query.isEmpty() || 
             it.title.contains(query, ignoreCase = true) || 
             it.description.contains(query, ignoreCase = true) ||
             it.standingSpot.contains(query, ignoreCase = true) ||
             it.aimSpot.contains(query, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Playbook checklists
    val playbookList = MutableStateFlow<List<PlaybookItem>>(emptyList())

    // Chat streams
    val chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "gemini",
                text = "Welcome to the Standoff 2 Tactical Adviser! I am trained in Sandstone, Hanami, Rust, Prison, Dune, Breeze, and Provinces. Ask me about execution strategies or standard utilities!"
            )
        )
    )
    val isChatLoading = MutableStateFlow(false)

    // Actions
    fun insertCustomNade(map: String, type: String, side: String, title: String, standing: String, aim: String, desc: String, video: String = "", image: String = "") {
        viewModelScope.launch {
            repository.insertCustomNade(
                CustomNadeEntity(
                    map = map,
                    type = type,
                    side = side,
                    title = title,
                    standingSpot = standing,
                    aimSpot = aim,
                    description = desc,
                    throwType = "Standing",
                    videoUrl = if (video.isBlank()) null else video,
                    imageUrl = if (image.isBlank()) null else image
                )
            )
        }
    }

    fun deleteCustomNade(idString: String) {
        val id = idString.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.deleteCustomNade(id)
            // Remove from favorites as well
            repository.toggleFavorite("custom_$id", false)
            // Remove from playbook
            playbookList.value = playbookList.value.filter { it.nadeKey != "custom_$id" }
        }
    }

    fun toggleFavorite(item: NadeUiItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.key, !item.isFavorite)
        }
    }

    // Playbook Management
    fun addNadeToPlaybook(item: NadeUiItem) {
        val current = playbookList.value
        if (current.none { it.nadeKey == item.key }) {
            playbookList.value = current + PlaybookItem(
                nadeKey = item.key,
                title = item.title,
                map = item.map,
                type = item.type,
                isReady = false
            )
        }
    }

    fun removeNadeFromPlaybook(nadeKey: String) {
        playbookList.value = playbookList.value.filter { it.nadeKey != nadeKey }
    }

    fun togglePlaybookItemReady(nadeKey: String) {
        playbookList.value = playbookList.value.map {
            if (it.nadeKey == nadeKey) it.copy(isReady = !it.isReady) else it
        }
    }

    fun clearPlaybook() {
        playbookList.value = emptyList()
    }

    // Gemini API Direct Communication
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return

        // 1. Add user message
        val currentChat = chatHistory.value
        chatHistory.value = currentChat + ChatMessage(sender = "user", text = text)
        isChatLoading.value = true

        viewModelScope.launch {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
                // Return a nice mock-simulation with instructions to add key
                chatHistory.value = chatHistory.value + ChatMessage(
                    sender = "gemini",
                    text = "🚨 **API Key Configuration Required** 🚨\n\nTo consult the active Gemini AI advisor, please register your private `GEMINI_API_KEY` inside the **Secrets panel** of Google AI Studio. " +
                            "\n\n*Temporary Tactical Suggestion*:\nSince you asked about *\"$text\"*, a standard play on ${selectedMap.value.ifBlank { "Standoff 2" }} is to throw standard cover smoke to block lines-of-sight and pop-flash round corners before executing!"
                )
                isChatLoading.value = false
                return@launch
            }

            try {
                // Compile conversation history
                val contents = chatHistory.value.takeLast(10).map { msg ->
                    GeminiContent(
                        role = if (msg.sender == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                }

                val systemInstructionText = "You are a Standoff 2 Tactical Esports Adviser. " +
                        "Help the player with tactical setups, team executes, counters, economizing, callouts, and advice on Standoff 2 maps (Sandstone, Hanami, Rust, Prison, Dune, Breeze, Provinces, etc.). " +
                        "Format your answer beautifully using markdown. Keep responses informative, precise, and concise."

                val response = RetrofitClient.service.generateContent(
                    apiKey = key,
                    request = GeminiRequest(
                        contents = contents,
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
                    )
                )

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Synthesizing calculations... Please try again."

                chatHistory.value = chatHistory.value + ChatMessage(sender = "gemini", text = replyText)
            } catch (e: Exception) {
                chatHistory.value = chatHistory.value + ChatMessage(
                    sender = "gemini",
                    text = "⚠️ **Error connecting to Tactical Adviser**: ${e.localizedMessage ?: "Network Timeout"}. Please check your internet connection or verify your API key."
                )
            } finally {
                isChatLoading.value = false
            }
        }
    }

    fun submitFeedback(type: String, rating: Int, message: String) {
        viewModelScope.launch {
            repository.insertFeedback(
                FeedbackEntity(
                    type = type,
                    rating = rating,
                    message = message
                )
            )
        }
    }

    fun clearAllFeedbacks() {
        viewModelScope.launch {
            repository.clearFeedbacks()
        }
    }
}
