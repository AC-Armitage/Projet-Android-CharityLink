package com.fpl.charitylink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fpl.charitylink.ui.screens.LoginScreen
import com.fpl.charitylink.ui.screens.OnboardingScreen
import com.fpl.charitylink.ui.screens.AssociationHomeScreen
import com.fpl.charitylink.ui.screens.DonorHomeScreen
import com.fpl.charitylink.ui.screens.RegisterScreen
import com.fpl.charitylink.ui.screens.RoleSelectionScreen
import com.fpl.charitylink.ui.screens.SplashScreen

object CharityLinkDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val ROLE_SELECTION = "role_selection"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DONOR_HOME = "donor_home"
    const val ASSOCIATION_HOME = "association_home"
}

@Composable
fun CharityLinkNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CharityLinkDestinations.SPLASH
    ) {
        composable(CharityLinkDestinations.SPLASH) {
            SplashScreen(onGetStarted = {
                navController.navigate(CharityLinkDestinations.ONBOARDING)
            })
        }
        composable(CharityLinkDestinations.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(CharityLinkDestinations.ROLE_SELECTION)
                },
                onSkip = {
                    navController.navigate(CharityLinkDestinations.ROLE_SELECTION)
                }
            )
        }
        composable(CharityLinkDestinations.ROLE_SELECTION) {
            RoleSelectionScreen(
                onContinue = {
                    navController.navigate(CharityLinkDestinations.LOGIN)
                },
                onCreateAccount = {
                    navController.navigate(CharityLinkDestinations.REGISTER)
                }
            )
        }
        composable(CharityLinkDestinations.LOGIN) {
            LoginScreen(
                onLogin = {
                    navController.navigate(CharityLinkDestinations.DONOR_HOME)
                },
                onCreateAccount = {
                    navController.navigate(CharityLinkDestinations.REGISTER)
                }
            )
        }
        composable(CharityLinkDestinations.REGISTER) {
            RegisterScreen(
                onRegister = {
                    navController.navigate(CharityLinkDestinations.DONOR_HOME)
                },
                onBackToLogin = {
                    navController.navigate(CharityLinkDestinations.LOGIN)
                }
            )
        }
        composable(CharityLinkDestinations.DONOR_HOME) {
            DonorHomeScreen()
        }
        composable(CharityLinkDestinations.ASSOCIATION_HOME) {
            AssociationHomeScreen()
        }
    }
}
