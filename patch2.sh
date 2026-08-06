sed -i '/override suspend fun doWork(): Result = withContext(Dispatchers.IO) {/a \
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {\
            val channel = NotificationChannel("download_channel", "Model Downloads", NotificationManager.IMPORTANCE_LOW)\
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager\
            manager.createNotificationChannel(channel)\
        }\
        val notification = NotificationCompat.Builder(applicationContext, "download_channel")\
            .setContentTitle("Downloading Model")\
            .setContentText("Preparing download...")\
            .setSmallIcon(android.R.drawable.stat_sys_download)\
            .setOngoing(true)\
            .build()\
        setForeground(ForegroundInfo(1001, notification))\
' app/src/main/java/com/example/backend/hf/HFDownloadWorker.kt
