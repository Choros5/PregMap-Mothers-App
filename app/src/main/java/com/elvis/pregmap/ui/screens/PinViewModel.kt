package com.elvis.pregmap.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt
import com.google.firebase.firestore.SetOptions

// Represents the state of the PIN validation/creation
sealed class PinState {
    object Idle : PinState()
    object Loading : PinState()
    object Success : PinState()
    data class Error(val message: String) : PinState()
}

class PinViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val _pinState = MutableStateFlow<PinState>(PinState.Idle)
    val pinState: StateFlow<PinState> = _pinState
    
    // PIN cache to avoid database queries
    private var cachedPinHash: String? = null
    private var currentUserId: String? = null
    
    // SharedPreferences for persistent caching
    private var prefs: SharedPreferences? = null
    
    fun initializePrefs(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("pin_cache", Context.MODE_PRIVATE)
        }
    }

    fun savePin(pin: String) {
        viewModelScope.launch {
            _pinState.value = PinState.Loading
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _pinState.value = PinState.Error("User not logged in.")
                    return@launch
                }
                
                // Hash the PIN before storing
                val pinHash = BCrypt.hashpw(pin, BCrypt.gensalt())
                
                // Store PIN hash (patient ID is already stored during registration)
                db.collection("users").document(userId)
                    .set(mapOf("pinHash" to pinHash), SetOptions.merge())
                    .await()
                
                // Cache the PIN hash for future use (both memory and persistent)
                cachedPinHash = pinHash
                currentUserId = userId
                prefs?.edit()?.apply {
                    putString("cached_pin_hash_$userId", pinHash)
                    putString("current_user_id", userId)
                    putBoolean("user_has_registered_$userId", true)
                    apply()
                }
                
                println("✅ PIN saved successfully and cached persistently")
                _pinState.value = PinState.Success
            } catch (e: Exception) {
                _pinState.value = PinState.Error("Failed to save PIN: ${e.message}")
            }
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            _pinState.value = PinState.Loading
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _pinState.value = PinState.Error("User not logged in.")
                    return@launch
                }

                // Check if we have a cached PIN hash for this user (memory first, then persistent)
                val storedHash = if (userId == currentUserId && cachedPinHash != null) {
                    println("✅ Using memory cached PIN hash")
                    cachedPinHash
                } else {
                    // Try to get from persistent cache
                    val persistentHash = prefs?.getString("cached_pin_hash_$userId", null)
                    if (persistentHash != null) {
                        println("✅ Using persistent cached PIN hash")
                        cachedPinHash = persistentHash
                        currentUserId = userId
                        persistentHash
                    } else {
                        println("🔍 Fetching PIN hash from database")
                        val userDoc = db.collection("users").document(userId).get().await()
                        val hash = userDoc.getString("pinHash")
                        
                        // Cache the PIN hash for future use (both memory and persistent)
                        if (hash != null) {
                            cachedPinHash = hash
                            currentUserId = userId
                            prefs?.edit()?.apply {
                                putString("cached_pin_hash_$userId", hash)
                                putString("current_user_id", userId)
                                putBoolean("user_has_registered_$userId", true)
                                apply()
                            }
                            println("✅ PIN hash cached persistently for future use")
                        }
                        
                        hash
                    }
                }

                if (storedHash == null) {
                    _pinState.value = PinState.Error("No PIN has been set for this account.")
                    return@launch
                }

                if (BCrypt.checkpw(pin, storedHash)) {
                    println("✅ PIN verified successfully")
                    _pinState.value = PinState.Success
                } else {
                    _pinState.value = PinState.Error("Invalid PIN.")
                }
            } catch (e: Exception) {
                _pinState.value = PinState.Error("An error occurred: ${e.message}")
            }
        }
    }
    
    // Check if user has registered without database query
    fun hasUserRegistered(userId: String): Boolean {
        // Check memory cache first
        if (userId == currentUserId && cachedPinHash != null) {
            return true
        }
        
        // Check persistent cache
        val hasRegistered = prefs?.getBoolean("user_has_registered_$userId", false) ?: false
        if (hasRegistered) {
            // Also load the PIN hash into memory cache
            val persistentHash = prefs?.getString("cached_pin_hash_$userId", null)
            if (persistentHash != null) {
                cachedPinHash = persistentHash
                currentUserId = userId
                println("✅ Loaded cached PIN hash for user $userId")
            }
        }
        
        return hasRegistered
    }
    
    // Clear cache when user changes or logs out
    fun clearCache() {
        cachedPinHash = null
        currentUserId = null
        // Note: We don't clear persistent cache on sign out to maintain performance
        // Persistent cache will be cleared when user actually changes or app is uninstalled
        println("🗑️ Memory PIN cache cleared")
    }
    
    // Clear persistent cache (for when user actually changes)
    fun clearPersistentCache() {
        val userId = currentUserId
        if (userId != null) {
            prefs?.edit()?.apply {
                remove("cached_pin_hash_$userId")
                remove("user_has_registered_$userId")
                apply()
            }
        }
        clearCache()
        println("🗑️ Persistent PIN cache cleared")
    }
} 