package com.example.thaparpg_gpt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWithDrawer(viewModel: ChatViewModel, navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        viewModel.clearMessages()
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()
                DrawerItem("Feedback") {  navController.navigate("feedback") }
                DrawerItem("Contact Us") { /* TODO */ }
                DrawerItem("Logout") {
                    navController.navigate("login") {
                        popUpTo("chat") { inclusive = true }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Thapar PG-GPT") },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Open Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate("profile") }) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1351B0),
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->

                chatpage(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}
@Composable
fun chatpage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    val messages = viewModel.messages

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.uber),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(), // So the list moves when keyboard appears
                    reverseLayout = true
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                }
            }
            // This stays fixed at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF90CAF9), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                MessageInput(
                    onMessageSend = { message ->
                        viewModel.sendMessage(message, true)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit, modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("") }
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type a message...") },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF0F0F0),
                unfocusedContainerColor = Color(0xFFF0F0F0),
                disabledContainerColor = Color(0xFFF0F0F0),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp)
        )

        IconButton(
            onClick = {
                onMessageSend(message)
                message = ""
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = " Send"
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) Color(0xFF69BEEF) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(text = message.text, color = Color.Black)
        }
    }
}



/*package com.example.thaparpg_gpt

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun chatpage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    val messages = viewModel.messages

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {

        Image(
            painter = painterResource(id = R.drawable.bg4),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )


        Column(
            modifier = Modifier
                .fillMaxSize()


        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                    reverseLayout = true
                ) {
                    items(viewModel.messages.reversed()) { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(8.dp)
                        )

                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Color(0xFF90CAF9),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
        ) {
            MessageInput(
                onMessageSend = { viewModel.sendMessage(it) },
                modifier = Modifier.fillMaxWidth()
            )

        }


    }
}
    @Composable
    fun MessageInput(onMessageSend: (String) -> Unit, modifier: Modifier = Modifier) {

        var message by remember {
            mutableStateOf("")
        }
        Row(
            modifier = modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically

        ){


            TextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Type a message...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
   /*         OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = message,
                onValueChange = {
                    message = it
                },
                label = { Text("Type a message...") },
                singleLine= true
            )
            */

            IconButton(
                onClick = {
                    onMessageSend(message)
                    message = ""
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = " Send"
                )
            }
        }
    }




//Box(
//        modifier= Modifier
//            .fillMaxWidth()
//            .background(Color(0xFFE1BEE7))
//    )
//    {
//
//    Text(modifier= Modifier, text ="Thapar PG-GPT")}



/*MessageInput(
                onMessageSend = {
                    viewModel.sendMessage(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) */*/