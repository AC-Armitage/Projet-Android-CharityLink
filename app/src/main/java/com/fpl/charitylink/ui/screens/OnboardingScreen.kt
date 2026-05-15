package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fpl.charitylink.ui.theme.SurfaceContainerLow
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
    val imageUrl: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Give with confidence",
            body = "Discover charities and local causes in your community.",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDBLZi9sfzV4tvUju7EJTM927MzEV7gnO_Hor5NUvlbtuDV3DnPjOT8YoaFQ9iTFKcc2VaZmmZ7wKCDBM3QjFF5je5SV6s_8Rusgxd31Dvp2tivD09hrTnAk0VQ0JsAVLimUUWsAXBbAt1qSUTW5EKY5ZWgN3tgy6VA8VSeiVG9Ask080OzUw70GmNhFAaojcu02oYR7QpsVgDKtqQ0jJ5ANN1gkfBJ-QxgVCmMjx-uGIQte0OFamipe46UBQ5f_HZizq5Xbp2eI-0I"
        ),
        OnboardingPage(
            title = "Donate money, clothes, or food",
            body = "Choose the way you want to give based on current needs.",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDZhEKmIo11AxmiXNoHDr88_EEBjMmMJ579c7XLKWYbUPXmYq_xjSYgTj38AQFA_weM0ANcacaFoJvdAWsFSUF9x--229wOHDn7k42Oay_BYQJKvASy5_Sw5Ar5oqb9toFO8VGFpElkcNf8TEAur2psTe5mLZFBVdGgd2uNVetKEer8bFNVxpw0u0keGW2B1VKAiEeE7CuGJxZFmLmxJVeanicSKBUPwNjxHNsE6qOEfP1JG0xEeVXlAb9F6Lq88ilJFEsiVtgvziLV"
        ),
        OnboardingPage(
            title = "Join the community",
            body = "See how your contributions change lives with real-time updates.",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlZld0B8k7dEwnRVBWXpZXjNUGPO8K90rLqbXC9YBkwTXR0_nk-f7dONJkgUHW1YAfg1gGM_FsIt9fp_E32UHdqNyQMn1dbKB7vUORGNwR19pC4o36OEygd5FbHCBJitqlgztl-PciuoL09tUpm8OjYwO2B1rK0Ld2dMifsvsFx5V8l6iZrrBM15IUbY4y7G1PPitto2oU65n48O0Sk0zvNcCw9f8bqCpwzX5m-CMBAP2slPGVqz3G-G9EGkVG0g_7EEfHmP1bGrkP"
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CharityLink",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                            .padding(16.dp),
                        color = SurfaceContainerLow,
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        AsyncImage(
                            model = page.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = page.body,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 24.dp, top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == pagerState.currentPage
                        val width = if (isSelected) 32.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .width(width)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isLastPage = pagerState.currentPage == pages.lastIndex
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Get started" else "Next",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
