package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = AsteroidAdapter(AsteroidClickListener { asteroid ->
            viewModel.displayAsteroidDetails(asteroid)
        })
        viewModel.navigateAsteroidDetails.observe(viewLifecycleOwner, Observer { asteroid ->
            asteroid?.let {
                this.findNavController().navigate(
                    MainFragmentDirections
                        .actionShowDetail(asteroid)
                )
                viewModel.doneNavigating()
            }
        })
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.asteroidRepository.updateFilter(
            when (item.itemId) {
                R.id.show_week_menu -> AsteroidsFilter.SHOW_WEEK
                R.id.show_today_menu -> AsteroidsFilter.SHOW_TODAY
                else -> AsteroidsFilter.SHOW_ALL
            }
        )
        return true
    }
}
