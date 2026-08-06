sed -i '/setProgress(workDataOf("PROGRESS" to (downloadedBytes.toFloat() \/ totalBytes \* 100).toInt()))/a \
                            val progress = (downloadedBytes.toFloat() / totalBytes * 100).toInt()\
                            val updateNotification = NotificationCompat.Builder(applicationContext, "download_channel")\
                                .setContentTitle("Downloading Model")\
                                .setContentText("Progress: $progress%")\
                                .setProgress(100, progress, false)\
                                .setSmallIcon(android.R.drawable.stat_sys_download)\
                                .setOngoing(true)\
                                .build()\
                            setForeground(ForegroundInfo(1001, updateNotification))\
' app/src/main/java/com/example/backend/hf/HFDownloadWorker.kt
