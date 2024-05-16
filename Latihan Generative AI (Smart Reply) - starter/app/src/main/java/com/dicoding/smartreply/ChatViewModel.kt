package com.dicoding.smartreply

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplyGenerator
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.TextMessage
import java.lang.Exception

class ChatViewModel : ViewModel() {

    private val anotherUserID = "101"

    private val _chatHistory = MutableLiveData<ArrayList<Message>>()
    val chatHistory: LiveData<ArrayList<Message>> = _chatHistory

    private val _pretendingAsAnotherUser = MutableLiveData<Boolean>()
    val pretendingAsAnotherUser: LiveData<Boolean> = _pretendingAsAnotherUser

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val smartReply : SmartReplyGenerator = SmartReply.getClient()

//    MediatorLiveData adalah sebuah LiveData yang dapat kita gunakan untuk
//    melakukan aksi observe pada objek LiveData lainnya
//    dan menghasilkan data baru jika terdapat perubahan nilai pada objek-objek tersebut.
    private val _smartReplyOptions = MediatorLiveData<List<SmartReplySuggestion>>()
    val smartReplyOptions: LiveData<List<SmartReplySuggestion>> = _smartReplyOptions

    init {
        initSmartReplyOptionsGenerator()
        _pretendingAsAnotherUser.value = false
    }

    fun switchUser() {
        clearSmartReplyOptions()
        val value = _pretendingAsAnotherUser.value!!
        _pretendingAsAnotherUser.value = !value
    }

    fun setMessages(messages: ArrayList<Message>) {
        clearSmartReplyOptions()
        _chatHistory.value = messages
    }

    fun addMessage(message: String) {

        val user = _pretendingAsAnotherUser.value!!

        var list: ArrayList<Message> = chatHistory.value ?: ArrayList()
        list.add(Message(message, !user, System.currentTimeMillis() ))

        clearSmartReplyOptions()
        _chatHistory.value = list
    }

//    Tempat terjadinya proses inferensi model ML pada aplikasi
//    Metode ini bertugas untuk meneruskan data masukan yang didapat dari aplikasi untuk diproses oleh SmartReplyGenerator
//    sehingga mendapatkan data keluaran berupa opsi jawaban balasan.
    private fun generateSmartReplyOptions(
        messages: List<Message>,
        isPretendingAnotherUser: Boolean
    ): Task<List<SmartReplySuggestion>> {
        val lastMessage = messages.last()

        if (lastMessage.isLocalUser != isPretendingAnotherUser) {
            return Tasks.forException(Exception("Tidak menjalankan smart reply!"))
        }

        val chatConversations = ArrayList<TextMessage>()
        for (message in messages) {
            if (message.isLocalUser != isPretendingAnotherUser) {
                chatConversations.add(TextMessage.createForLocalUser(message.text, message.timestamp))
            } else {
                chatConversations.add(
                    TextMessage.createForRemoteUser(message.text, message.timestamp, anotherUserID)
                )
            }
        }

//    Untuk gunakan SmartReplyGenerator kita panggil method suggerReplies
//    dengan data percakapan chatConversations
        return smartReply
            .suggestReplies(chatConversations)
            .continueWith{ task ->
                val result = task.result
                when(result.status) {
                    SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE ->
                        _errorMessage.value = "Unable to generate options due to a non-English language was used"
                    SmartReplySuggestionResult.STATUS_NO_REPLY ->
                        _errorMessage.value = "Unable to generate options due to no appropriate response found"
                }
                result.suggestions
            }
            .addOnFailureListener{ e ->
                _errorMessage.value = "An error has occured on Smart Reply Instance"
            }
    }

//    menginisialisasi sebuah objek MediatorLiveData yang kita gunakan pada aplikasi
//    untuk melakukan observasi pada objek LiveData pretendingAsAnotherUser dan chatHistory.
    private fun initSmartReplyOptionsGenerator() {
        _smartReplyOptions.addSource(pretendingAsAnotherUser) {isPretendingAsAnotherUser ->
            val list = chatHistory.value

            if (list.isNullOrEmpty()) {
                return@addSource
            } else {
                generateSmartReplyOptions(list, isPretendingAsAnotherUser)
                    .addOnSuccessListener { result ->
                        _smartReplyOptions.value = result
                    }
            }
        }

//        Kedua LiveData tersebut perlu di-observe karena kita ingin aplikasi memberikan opsi jawaban
//        balasan saat pengguna aplikasi berganti ataupun saat terdapat pesan baru pada percakapan.
        _smartReplyOptions.addSource(chatHistory){ conversations ->
            val isPretendingAsAnotherUser = pretendingAsAnotherUser.value

            if (isPretendingAsAnotherUser != null && conversations.isNullOrEmpty()) {
                return@addSource
            } else {
                generateSmartReplyOptions(conversations, isPretendingAsAnotherUser!!)
                    .addOnSuccessListener { result ->
                        _smartReplyOptions.value = result
                    }
            }
        }
    }

    private fun clearSmartReplyOptions() {
        _smartReplyOptions.value = ArrayList()
    }

    override fun onCleared() {
        super.onCleared()
        smartReply.close()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }

}
