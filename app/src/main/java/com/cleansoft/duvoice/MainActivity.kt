package com.cleansoft.duvoice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cleansoft.duvoice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar Splash Screen ANTES do super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar destinos de nível superior (sem botão de voltar)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.recordFragment, R.id.statsFragment, R.id.settingsFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        // Ocultar bottom navigation em algumas telas
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.isVisible = when (destination.id) {
                R.id.playerFragment -> false
                else -> true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}