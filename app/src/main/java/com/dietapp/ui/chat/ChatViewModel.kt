package com.dietapp.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.dietapp.data.api.FoodItem

class ChatViewModel : ViewModel() {
    val messageHistory = mutableStateListOf<ChatMessage>()
    var hasShownGreeting = false
    var pendingFoods: List<FoodItem> = emptyList()
}
