package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.KirtanEntity
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.ThemeMode
import com.example.ui.util.Localization

@Composable
fun DevotionalHeader(
    currentKirtan: KirtanEntity?,
    allKirtans: List<KirtanEntity>,
    currentThemeMode: ThemeMode,
    currentLanguage: AppLanguage,
    onSelectKirtan: (Long?) -> Unit,
    onAddNewKirtan: () -> Unit,
    onShareReport: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current
    val strings = Localization.get(currentLanguage)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Devotional Section Header Label
            Text(
                text = strings.devotionalManager,
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = strings.appName,
                        color = colors.textPrimary,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = strings.appSubtitle,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Add Kirtan Action
                IconButton(
                    onClick = onAddNewKirtan,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .testTag("add_kirtan_header_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = strings.addNewKirtan,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kirtan Selector Pill / Dropdown
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { menuExpanded = true }
                        .testTag("kirtan_selector_pill"),
                    color = colors.cardBg,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentKirtan?.name ?: strings.allKirtans,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (currentKirtan != null && currentKirtan.location.isNotBlank()) {
                                    Text(
                                        text = currentKirtan.location,
                                        color = colors.textSecondary,
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.switchKirtan,
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = strings.switchKirtan,
                                tint = colors.primary
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(colors.cardBg)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "🌐 ${strings.allKirtans}",
                                fontWeight = if (currentKirtan == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentKirtan == null) colors.primary else colors.textPrimary
                            )
                        },
                        onClick = {
                            onSelectKirtan(null)
                            menuExpanded = false
                        }
                    )

                    allKirtans.forEach { k ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "🌺 ${k.name}",
                                        fontWeight = if (currentKirtan?.id == k.id) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentKirtan?.id == k.id) colors.primary else colors.textPrimary
                                    )
                                    if (k.location.isNotBlank()) {
                                        Text(
                                            text = k.location,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectKirtan(k.id)
                                menuExpanded = false
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.addNewKirtan,
                                    color = colors.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onAddNewKirtan()
                        }
                    )
                }
            }
        }
    }
}
