package com.kevinxu.remaidata.ui.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.model.Rating
import com.kevinxu.remaidata.utils.ConvertUtils
import java.util.Locale
import kotlin.math.floor

@Composable
fun RatingCalculator(
    onInvalidTargetRating: () -> Unit,
    onCalculateClick: () -> Unit
) {
    var targetRating by remember { mutableStateOf("") }
    var songLevel by remember { mutableStateOf("") }
    var songAchievement by remember { mutableStateOf("") }
    val results = remember { mutableStateListOf<Rating>() }

    val singleSongRating = calculateSingleSongRating(songLevel, songAchievement)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.target_rating),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetRating,
                        onValueChange = { targetRating = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                    Button(
                        onClick = {
                            onCalculateClick()
                            val calculated = calculateTargetRatings(targetRating)
                            if (calculated == null) {
                                onInvalidTargetRating()
                            } else {
                                results.clear()
                                results.addAll(calculated)
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.calculate))
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            RatingResultsCard(results = results)
        }

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.single_rating_calculator),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.rating_sheet_desc_1),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.rating_sheet_desc_2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = songLevel,
                        onValueChange = { songLevel = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(text = stringResource(R.string.song_level_hint)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = songAchievement,
                        onValueChange = { songAchievement = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(text = stringResource(R.string.song_achievement_hint)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                    )
                }
                Text(
                    text = stringResource(R.string.single_rating_result, singleSongRating),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RatingResultsCard(results: List<Rating>) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultsHeader(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.song_level)
                )
                ResultsHeader(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.achievement)
                )
                ResultsHeader(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.single_rating)
                )
                ResultsHeader(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.total_rating)
                )
            }
            results.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResultsCell(modifier = Modifier.weight(1f), text = item.innerLevel.toString())
                    ResultsCell(modifier = Modifier.weight(1f), text = item.achi)
                    ResultsCell(modifier = Modifier.weight(1f), text = item.rating.toString())
                    ResultsCell(modifier = Modifier.weight(1f), text = item.total.toString())
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ResultsCell(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun calculateSingleSongRating(
    songLevel: String,
    songAchievement: String
): Int {
    val level = songLevel.toFloatOrNull()
    val achievement = songAchievement.toFloatOrNull()
    if (level == null || achievement == null) return 0
    return ConvertUtils.achievementToRating(
        (level * 10).toInt(),
        (achievement * 10000).toInt()
    )
}

fun calculateTargetRatings(targetRatingText: String): List<Rating>? {
    val targetRating = targetRatingText.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val rating = targetRating / 50

    val minLv = getReachableLevel(rating)
    val map = mutableMapOf<Int, Int>()
    val list = mutableListOf<Rating>()

    for (i in 150 downTo minLv) {
        when (val reachableAchievement = getReachableAchievement(i, rating)) {
            800000, 900000, 940000 -> map[reachableAchievement] = i
            in 970000..1010000 -> map[reachableAchievement] = i
        }
    }

    map.forEach {
        list.add(
            Rating(
                (it.value / 10f),
                String.format(Locale.getDefault(), "%.4f%%", it.key / 10000f),
                ConvertUtils.achievementToRating(it.value, it.key),
                ConvertUtils.achievementToRating(it.value, it.key) * 50
            )
        )
    }

    return list
}

private fun getReachableLevel(rating: Int): Int {
    for (i in 10..150) {
        if (rating < ConvertUtils.achievementToRating(i, 1005000)) {
            return i
        }
    }
    return 0
}

private fun getReachableAchievement(level: Int, rating: Int): Int {
    var maxAchi = 1010000
    var minAchi = 0
    var tempAchi: Int

    if (ConvertUtils.achievementToRating(level, 1005000) < rating) {
        return 1010001
    }

    for (n in 0..20) {
        if (maxAchi - minAchi >= 2) {
            tempAchi = floor((maxAchi.toDouble() + minAchi) / 2).toInt()

            if (ConvertUtils.achievementToRating(level, tempAchi) < rating) {
                minAchi = tempAchi
            } else {
                maxAchi = tempAchi
            }
        }
    }
    return maxAchi
}
