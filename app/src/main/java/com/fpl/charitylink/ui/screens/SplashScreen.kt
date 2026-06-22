package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fpl.charitylink.R
import com.fpl.charitylink.ui.theme.PrimaryContainer
import com.fpl.charitylink.ui.theme.PrimaryFixed
import com.fpl.charitylink.ui.theme.PrimaryFixedDim

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryContainer)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Handshake,
                        contentDescription = null,
                        tint = PrimaryContainer,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(text = stringResource(R.string.get_started), style = MaterialTheme.typography.labelLarge)
            }
        }

        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(PrimaryFixed.copy(alpha = 0.15f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(PrimaryFixedDim.copy(alpha = 0.15f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(PrimaryFixedDim.copy(alpha = 0.12f))
        )
    }
}
