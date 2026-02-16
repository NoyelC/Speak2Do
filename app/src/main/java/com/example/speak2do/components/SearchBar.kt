package com.example.speak2do.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.*

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    resultCount: Int = -1
) {
    Column {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = WhiteText, fontSize = 16.sp),
            modifier = Modifier.semantics {
                contentDescription = "Search tasks"
            },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(CardBackground, RoundedCornerShape(26.dp))
                        .padding(horizontal = Dimens.SpacingLg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Search tasks...", color = MutedText, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                    // Clear button
                    androidx.compose.animation.AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(Dimens.MinTouchTarget)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        )

        // Result count feedback
        androidx.compose.animation.AnimatedVisibility(
            visible = query.isNotBlank() && resultCount >= 0,
            enter = fadeIn(tween(200)) + expandVertically(),
            exit = fadeOut(tween(150)) + shrinkVertically()
        ) {
            Text(
                text = when (resultCount) {
                    0 -> "No results found"
                    1 -> "1 result found"
                    else -> "$resultCount results found"
                },
                color = MutedText,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = Dimens.SpacingLg, top = Dimens.SpacingXs)
            )
        }
    }
}
