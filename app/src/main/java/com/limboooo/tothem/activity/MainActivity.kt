package com.limboooo.tothem.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.proxyFragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.github.fragivity.loadRoot
import com.limboooo.tothem.R
import com.limboooo.tothem.databinding.ActivityMainBinding
import com.limboooo.tothem.fragment.FragmentMainList
import com.limboooo.tothem.viewmodel.MyViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


/**
 * developing branch
 */
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            viewModel.dataList.first()
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        proxyFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        navHostFragment.loadRoot(FragmentMainList::class)
    }

}

