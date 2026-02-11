package com.example.speak2do.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.WhiteText
import com.example.speak2do.ui.theme.PrimaryCyan

@Composable
fun TasksScreen() {

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("All", "Today", "Upcoming")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {

        Text(
            text = "Tasks",
            fontSize = 24.sp,
            color = WhiteText
        )

        Spacer(Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkBackground,
            contentColor = PrimaryCyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index)
                                PrimaryCyan
                            else
                                MutedText
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AllTasksContent()
            1 -> TodayTasksContent()
            2 -> UpcomingTasksContent()
        }
    }
}

@Composable
fun AllTasksContent() {
    Text("All Tasks", color = WhiteText)
}

@Composable
fun TodayTasksContent() {
    Text("Today's Tasks", color = WhiteText)
}

@Composable
fun UpcomingTasksContent() {
    Text("Upcoming Tasks", color = WhiteText)
}

