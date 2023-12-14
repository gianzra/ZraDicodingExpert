package com.gianzra.expert.submission.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gianzra.expert.submission.databinding.ActivityHomeBinding
import com.gianzra.expert.submission.R
import com.gianzra.expert.submission.movies.MoviesFragment
import com.gianzra.expert.submission.tvshows.TvShowsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        loadFragment(MoviesFragment())
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationContainer.setNavigationChangeListener { _, position ->
            when (position) {
                0 -> loadFragment(MoviesFragment())
                1 -> loadFragment(TvShowsFragment())
                2 -> moveToFavoriteFragment()
            }
        }
    }

    private fun moveToFavoriteFragment() {
        instantiateFragment<Fragment>("com.gianzra.expert.favorite.FavoriteFragment")?.let {
            loadFragment(it)
        }
    }

    private fun <T : Fragment> loadFragment(fragment: T) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameContainer, fragment)
            setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }
    }

    private inline fun <reified T : Fragment> instantiateFragment(className: String): T? {
        return try {
            Class.forName(className).newInstance() as T
        } catch (e: Exception) {
            showToast("Module not found")
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
