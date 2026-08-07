sed -i '/fun ChatBubble(message: ChatMessageEntity) {/,/    }/c\
@Composable\
fun ChatBubble(message: ChatMessageEntity) {\
    val isUser = message.role == "user"\
    val clipboardManager = LocalClipboardManager.current\
    val context = LocalContext.current\
    Row(\
        modifier = Modifier\
            .fillMaxWidth()\
            .padding(vertical = 4.dp),\
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start\
    ) {\
        if (!isUser) {\
            Box(\
                modifier = Modifier\
                    .size(32.dp)\
                    .clip(CircleShape)\
                    .background(AccentPurple),\
                contentAlignment = Alignment.Center\
            ) {\
                Text("AI", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)\
            }\
            Spacer(modifier = Modifier.width(8.dp))\
        }\
        Column {\
            Surface(\
                color = if (isUser) AccentTeal else SurfaceDark,\
                shape = RoundedCornerShape(\
                    topStart = 16.dp,\
                    topEnd = 16.dp,\
                    bottomStart = if (isUser) 16.dp else 4.dp,\
                    bottomEnd = if (isUser) 4.dp else 16.dp\
                ),\
                shadowElevation = if (isUser) 0.dp else 2.dp,\
                modifier = Modifier.widthIn(max = 280.dp)\
            ) {\
                Text(\
                    text = message.content,\
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),\
                    color = if (isUser) Color.White else TextPrimary,\
                    style = MaterialTheme.typography.bodyLarge\
                )\
            }\
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {\
                IconButton(onClick = {\
                    clipboardManager.setText(buildAnnotatedString { append(message.content) })\
                }, modifier = Modifier.size(24.dp)) {\
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))\
                }\
                Spacer(modifier = Modifier.width(8.dp))\
                IconButton(onClick = {\
                    val sendIntent: Intent = Intent().apply {\
                        action = Intent.ACTION_SEND\
                        putExtra(Intent.EXTRA_TEXT, message.content)\
                        type = "text/plain"\
                    }\
                    val shareIntent = Intent.createChooser(sendIntent, null)\
                    context.startActivity(shareIntent)\
                }, modifier = Modifier.size(24.dp)) {\
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(14.dp))\
                }\
            }\
        }\
        if (isUser) {\
            Spacer(modifier = Modifier.width(8.dp))\
            Box(\
                modifier = Modifier\
                    .size(32.dp)\
                    .clip(CircleShape)\
                    .background(MaterialTheme.colorScheme.secondary),\
                contentAlignment = Alignment.Center\
            ) {\
                Text("U", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)\
            }\
        }\
    }\
}' app/src/main/java/com/example/ui/screens/ChatScreen.kt
