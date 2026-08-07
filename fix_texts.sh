sed -i '115,120c\
                        Text(\
                            text = stateText,\
                            style = MaterialTheme.typography.bodySmall,\
                            color = if (modelState is com.example.backend.models.ModelState.Failed) Color.Red else TextSecondary\
                        )' app/src/main/java/com/example/ui/screens/ChatScreen.kt
