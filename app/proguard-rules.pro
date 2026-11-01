# Minimal Music Widget Proguard rules
-keep class com.example.minimalmusicwidget.** { *; }
-keepclassmembers class * extends android.appwidget.AppWidgetProvider {
    public void onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[]);
    public void onReceive(android.content.Context, android.content.Intent);
}
-keep class * extends android.service.notification.NotificationListenerService { *; }
