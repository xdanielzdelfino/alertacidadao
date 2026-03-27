# ProGuard/R8 rules para AlertaCidadao App

# Manter classes do Android
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }
-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

# Room Database - manter entidades e DAOs
-keep class com.alertacidadao.app.entity.** { *; }
-keep class com.alertacidadao.app.dao.** { *; }
-keepclassmembers class com.alertacidadao.app.database.** { *; }

# ViewModels
-keep class com.alertacidadao.app.viewmodel.** { *; }
-keepclassmembers class com.alertacidadao.app.viewmodel.** { *; }

# Fragments e Activities
-keep class com.alertacidadao.app.** extends androidx.fragment.app.Fragment { *; }
-keep class com.alertacidadao.app.** extends androidx.appcompat.app.AppCompatActivity { *; }

# Serialização
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Glide (Image Loading)
-keep class com.bumptech.glide.** { *; }
-keep class * implements com.bumptech.glide.module.GlideModule

# Navigation
-keepnames class * extends androidx.navigation.Navigator
-keepnames class * implements androidx.navigation.NavArgs

# Webkit / WebView
-keep class android.webkit.** { *; }
-keep class * extends android.webkit.WebViewClient { *; }

# Play Services
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Logging - remover
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Otimizações gerais
-optimizationpasses 5
-verbose

# Manter nomes de arquivos originais
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
