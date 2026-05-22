package com.fpl.charitylink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
fun CharityLinkNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // If user is already logged in, skip to home
    val startDestination = if (authViewModel.currentUser != null)
        CharityLinkDestinations.DONOR_HOME
    else
        CharityLinkDestinations.SPLASH

    NavHost(navController = navController, startDestination = startDestination) {
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
                onContinue = { navController.navigate(CharityLinkDestinations.LOGIN) },
                onCreateAccount = { navController.navigate(CharityLinkDestinations.REGISTER) }
            )
        }
        composable(CharityLinkDestinations.LOGIN) {
            LoginScreen(
                onLogin = { navController.navigate(CharityLinkDestinations.DONOR_HOME) { popUpTo(0) } },
                onCreateAccount = { navController.navigate(CharityLinkDestinations.REGISTER) },
                authViewModel = authViewModel
            )
        }
        composable(CharityLinkDestinations.REGISTER) {
            RegisterScreen(
                onRegister = { navController.navigate(CharityLinkDestinations.DONOR_HOME) { popUpTo(0) } },
                onBackToLogin = { navController.navigate(CharityLinkDestinations.LOGIN) },
                authViewModel = authViewModel
            )
        }
        composable(CharityLinkDestinations.DONOR_HOME) {
            DonorHomeScreen(
                onProfileClick = {
                    navController.navigate(CharityLinkDestinations.PROFILE)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(CharityLinkDestinations.LOGIN) {
                        popUpTo(0)
                    }
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
                    navController.navigate(CharityLinkDestinations.LOGIN) {
                        popUpTo(0)
                    }
                },
                authViewModel = authViewModel
            )
        }
    }
}
