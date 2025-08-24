package com.example.docmat.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.app
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(@ApplicationContext ctx: Context): FirebaseStorage {
        // Ensure FirebaseApp is initialized
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx)
        }

        // Get storage bucket from google-services.json
        val bucket = Firebase.app.options.storageBucket
            ?: "docmat-app.appspot.com" // Fallback to correct default bucket format

        return try {
            // Try to get Firebase Storage instance with the correct bucket URL
            val storageUrl = if (bucket.startsWith("gs://")) bucket else "gs://$bucket"
            val instance = FirebaseStorage.getInstance(storageUrl)
            
            android.util.Log.i("FirebaseModule", "Firebase Storage configured with bucket: $storageUrl")
            instance
        } catch (e: Exception) {
            android.util.Log.w("FirebaseModule", "Failed to initialize custom storage bucket, using default", e)
            // Fallback to default instance
            FirebaseStorage.getInstance()
        }
    }
}
