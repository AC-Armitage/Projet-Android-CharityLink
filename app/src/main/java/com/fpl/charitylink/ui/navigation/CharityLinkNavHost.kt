package com.fpl.charitylink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fpl.charitylink.viewmodel.AuthState
import com.fpl.charitylink.viewmodel.AuthViewModel
import com.fpl.charitylink.ui.screens.*

object CharityLinkDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val ROLE_SELECTION = "role_selection"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DONOR_HOME = "donor_home"
    const val DONOR_EXPLORE = "donor_explore"
    const val DONOR_DONATIONS = "donor_donations"
    const val ASSOCIATION_HOME = "association_home"
    const val ASSOCIATION_DONATIONS = "association_donations"
    const val ASSOCIATION_DETAIL = "association_detail"
    const val NEED_DETAIL = "need_detail"
    const val DONATE = "donate"
    const val POST_NEED = "post_need"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val ALL_CAMPAIGNS = "all_campaigns"
    const val NOTIFICATIONS = "notifications"
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat"
    const val EDIT_NEED = "edit_need"
}

@Composable
fun CharityLinkNavHost(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // Handle navigation after auth success
    val authState by authViewModel.authState.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val role = (authState as AuthState.Success).role
            val destination = if (role == "association")
                CharityLinkDestinations.ASSOCIATION_HOME
            else
                CharityLinkDestinations.DONOR_HOME
            navController.navigate(destination) { popUpTo(0) }
            authViewModel.resetState()
        }
    }

    // If already logged in, fetch role and navigate
    LaunchedEffect(Unit) {
        if (authViewModel.currentUser != null) {
            authViewModel.fetchCurrentUserRole { role ->
                val destination = if (role == "association")
                    CharityLinkDestinations.ASSOCIATION_HOME
                else
                    CharityLinkDestinations.DONOR_HOME
                navController.navigate(destination) { popUpTo(0) }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.currentUser != null)
            CharityLinkDestinations.DONOR_HOME
        else
            CharityLinkDestinations.SPLASH
    ) {
        composable(CharityLinkDestinations.SPLASH) {
            SplashScreen(onGetStarted = { navController.navigate(CharityLinkDestinations.ONBOARDING) })
        }
        composable(CharityLinkDestinations.ONBOARDING) {
            OnboardingScreen(
                onFinish = { navController.navigate(CharityLinkDestinations.ROLE_SELECTION) },
                onSkip = { navController.navigate(CharityLinkDestinations.ROLE_SELECTION) }
            )
        }
        composable(CharityLinkDestinations.ROLE_SELECTION) {
            RoleSelectionScreen(
                onContinue = { role ->
                    navController.navigate("${CharityLinkDestinations.LOGIN}/$role")
                },
                onCreateAccount = { role ->
                    navController.navigate("${CharityLinkDestinations.REGISTER}/$role")
                }
            )
        }
        composable("${CharityLinkDestinations.EDIT_NEED}/{campaignId}") { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            PostNeedScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                campaignId = campaignId
            )
        }
        composable("${CharityLinkDestinations.LOGIN}/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "donor"
            LoginScreen(
                onLogin = { },
                onCreateAccount = {
                    navController.navigate("${CharityLinkDestinations.REGISTER}/$role")
                },
                role = role,
                authViewModel = authViewModel
            )
        }
        composable("${CharityLinkDestinations.REGISTER}/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "donor"
            RegisterScreen(
                onRegister = { },
                onBackToLogin = {
                    navController.navigate("${CharityLinkDestinations.LOGIN}/$role")
                },
                role = role,
                authViewModel = authViewModel
            )
        }
        composable(CharityLinkDestinations.DONOR_HOME) {
            DonorHomeScreen(
                onProfileClick = { navController.navigate(CharityLinkDestinations.PROFILE) },
                onSeeAllClick = { navController.navigate(CharityLinkDestinations.ALL_CAMPAIGNS) },
                onNotificationsClick = { navController.navigate(CharityLinkDestinations.NOTIFICATIONS) },
                onChatClick = { navController.navigate(CharityLinkDestinations.CHAT_LIST) },
                onExploreClick = { navController.navigate(CharityLinkDestinations.DONOR_EXPLORE) },
                onDonationsClick = { navController.navigate(CharityLinkDestinations.DONOR_DONATIONS) },
                onAssociationClick = { associationId ->
                    navController.navigate("${CharityLinkDestinations.ASSOCIATION_DETAIL}/$associationId")
                },
                onNeedClick = { needId ->
                    navController.navigate("${CharityLinkDestinations.NEED_DETAIL}/$needId")
                },
                onDonateClick = { navController.navigate(CharityLinkDestinations.ALL_CAMPAIGNS) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(CharityLinkDestinations.LOGIN + "/donor") { popUpTo(0) }
                }

            )
        }
        composable(CharityLinkDestinations.ASSOCIATION_HOME) {
            AssociationHomeScreen(
                authViewModel = authViewModel,
                onProfileClick = { navController.navigate(CharityLinkDestinations.PROFILE) },
                onEditNeedClick = { campaignId ->
                    navController.navigate("${CharityLinkDestinations.EDIT_NEED}/$campaignId")
                },
                onExploreClick = { navController.navigate(CharityLinkDestinations.DONOR_EXPLORE) },
                onNotificationsClick = { navController.navigate(CharityLinkDestinations.NOTIFICATIONS) },
                onChatClick = { navController.navigate(CharityLinkDestinations.CHAT_LIST) },
                onDonationsClick = { navController.navigate(CharityLinkDestinations.ASSOCIATION_DONATIONS) },
                onPostNeedClick = { navController.navigate(CharityLinkDestinations.POST_NEED) },
                onNeedClick = { needId ->
                    navController.navigate("${CharityLinkDestinations.NEED_DETAIL}/$needId")
                }
            )
        }
        composable(CharityLinkDestinations.DONOR_EXPLORE) {
            DonorExploreScreen(
                onAssociationClick = { associationId ->
                    navController.navigate("${CharityLinkDestinations.ASSOCIATION_DETAIL}/$associationId")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(CharityLinkDestinations.DONOR_DONATIONS) {
            DonationsScreen(
                isAssociation = false,
                onBack = { navController.popBackStack() }
            )
        }
        composable(CharityLinkDestinations.ASSOCIATION_DONATIONS) {
            DonationsScreen(
                isAssociation = true,
                onBack = { navController.popBackStack() }
            )
        }
        composable("${CharityLinkDestinations.ASSOCIATION_DETAIL}/{associationId}") { backStackEntry ->
            val associationId = backStackEntry.arguments?.getString("associationId") ?: ""
            AssociationDetailScreen(
                associationId = associationId,
                onBack = { navController.popBackStack() },
                onNeedClick = { needId ->
                    navController.navigate("${CharityLinkDestinations.NEED_DETAIL}/$needId")
                },
                onMessageClick = { chatId, otherUserId, otherUserName ->
                    val encodedName = java.net.URLEncoder.encode(otherUserName, "UTF-8")
                    navController.navigate("${CharityLinkDestinations.CHAT}/$chatId/$otherUserId/$encodedName")
                }
            )
        }
        composable("${CharityLinkDestinations.NEED_DETAIL}/{needId}") { backStackEntry ->
            val needId = backStackEntry.arguments?.getString("needId") ?: ""
            NeedDetailScreen(
                needId = needId,
                onBack = { navController.popBackStack() },
                onAssociationClick = { associationId ->
                    navController.navigate("${CharityLinkDestinations.ASSOCIATION_DETAIL}/$associationId")
                },
                onDonateClick = { needId ->
                    navController.navigate("${CharityLinkDestinations.DONATE}/$needId")
                }
            )
        }
        composable("${CharityLinkDestinations.DONATE}/{campaignId}") { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            DonateScreen(
                campaignId = campaignId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        composable(CharityLinkDestinations.POST_NEED) {
            PostNeedScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        composable(CharityLinkDestinations.PROFILE) {
            val role = authViewModel.cachedRole.collectAsState().value

            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(CharityLinkDestinations.LOGIN + "/donor") { popUpTo(0) }
                },
                onEditProfile = { navController.navigate(CharityLinkDestinations.EDIT_PROFILE) },
                onSettings = { navController.navigate(CharityLinkDestinations.SETTINGS) },
                onHomeClick = {
                    val destination = if (role == "association")
                        CharityLinkDestinations.ASSOCIATION_HOME
                    else
                        CharityLinkDestinations.DONOR_HOME
                    navController.navigate(destination) { popUpTo(0) }
                },
                onExploreClick = { /* TODO */ },
                onDonationsClick = { /* TODO */ },
                authViewModel = authViewModel
            )
        }
        composable(CharityLinkDestinations.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable(CharityLinkDestinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable(CharityLinkDestinations.ALL_CAMPAIGNS) {
            AllCampaignsScreen(
                onBack = { navController.popBackStack() },
                onCampaignClick = { campaignId ->
                    navController.navigate("${CharityLinkDestinations.NEED_DETAIL}/$campaignId")
                }
            )
        }
        composable(CharityLinkDestinations.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(CharityLinkDestinations.CHAT_LIST) {
            ChatListScreen(
                onBack = { navController.popBackStack() },
                onChatClick = { chatId, otherUserId, otherUserName ->
                    val encodedName = java.net.URLEncoder.encode(otherUserName, "UTF-8")
                    navController.navigate("${CharityLinkDestinations.CHAT}/$chatId/$otherUserId/$encodedName")
                }
            )
        }
        composable("${CharityLinkDestinations.CHAT}/{chatId}/{otherUserId}/{otherUserName}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val rawName = backStackEntry.arguments?.getString("otherUserName") ?: ""
            val otherUserName = java.net.URLDecoder.decode(rawName, "UTF-8")
            ChatScreen(
                chatId = chatId,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
