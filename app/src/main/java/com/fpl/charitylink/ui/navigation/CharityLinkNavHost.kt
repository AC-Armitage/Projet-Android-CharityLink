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
    const val ASSOCIATION_HOME = "association_home"
    const val PROFILE = "profile"
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
        composable("${CharityLinkDestinations.LOGIN}/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "donor"
            LoginScreen(
                onLogin = { },
                onCreateAccount = {
                    navController.navigate("${CharityLinkDestinations.REGISTER}/$role")
                },
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
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(CharityLinkDestinations.LOGIN + "/donor") { popUpTo(0) }
                }
            )
        }
        composable(CharityLinkDestinations.ASSOCIATION_HOME) {
            AssociationHomeScreen()
        }
        composable(CharityLinkDestinations.PROFILE) {
            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(CharityLinkDestinations.LOGIN + "/donor") { popUpTo(0) }
                },
                authViewModel = authViewModel
            )
        }
    }
}