package com.alertacidadao.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.alertacidadao.app.databinding.ActivityMainBinding
import com.alertacidadao.app.ui.setupSystemBars

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as? NavHostFragment
        if (navHostFragment == null) {
            Toast.makeText(this, "Falha ao carregar a navegação principal", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val navController = navHostFragment.navController

        setupBottomBar(navController)
        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddReportActivity::class.java))
        }
    }

    private fun setupBottomBar(navController: NavController) {
        binding.tabHome.setOnClickListener {
            navigateTo(navController, R.id.homeFragment)
        }
        binding.tabMap.setOnClickListener {
            navigateTo(navController, R.id.mapFragment)
        }
        binding.tabProfile.setOnClickListener {
            navigateTo(navController, R.id.profileFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomBar(destination)
        }

        updateBottomBar(navController.currentDestination)
    }

    private fun navigateTo(navController: NavController, destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(false)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()

        navController.navigate(destinationId, null, options)
    }

    private fun updateBottomBar(destination: NavDestination?) {
        val selectedId = destination?.id

        setTabState(
            tab = binding.tabHome,
            pill = binding.pillHome,
            icon = binding.iconHome,
            label = binding.labelHome,
            selected = selectedId == R.id.homeFragment
        )
        setTabState(
            tab = binding.tabMap,
            pill = binding.pillMap,
            icon = binding.iconMap,
            label = binding.labelMap,
            selected = selectedId == R.id.mapFragment
        )
        setTabState(
            tab = binding.tabProfile,
            pill = binding.pillProfile,
            icon = binding.iconProfile,
            label = binding.labelProfile,
            selected = selectedId == R.id.profileFragment
        )
    }

    private fun setTabState(
        tab: View,
        pill: View,
        icon: View,
        label: View,
        selected: Boolean
    ) {
        pill.alpha = if (selected) 1f else 0.8f
        label.visibility = View.VISIBLE
        label.alpha = if (selected) 1f else 0.75f
        icon.alpha = if (selected) 1f else 0.8f
        if (icon is android.widget.ImageView) {
            icon.imageTintList = getColorStateList(
                if (selected) R.color.primary else R.color.text_secondary
            )
        }
        if (label is android.widget.TextView) {
            label.setTextColor(
                getColorStateList(if (selected) R.color.primary else R.color.text_secondary)
            )
        }
        tab.isSelected = selected
    }
}
