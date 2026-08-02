package com.battlebucks.app.presentation.leaderboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.battlebucks.app.R
import com.battlebucks.app.databinding.FragmentLeaderboardBinding
import com.battlebucks.app.presentation.leaderboard.adapter.LeaderboardAdapter
import com.battlebucks.app.presentation.leaderboard.viewmodel.LeaderboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeaderboardViewModel by viewModels()
    private val adapter = LeaderboardAdapter(CURRENT_PLAYER_ID)
    private val scoreFormat = NumberFormat.getIntegerInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter
        binding.rvLeaderboard.setHasFixedSize(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.leaderboard.collectLatest { rows ->
                    binding.progressBar.isVisible = rows.isEmpty()
                    binding.tvEmpty.isVisible = false
                    binding.rvLeaderboard.isVisible = rows.isNotEmpty()

                    adapter.submitList(rows)

                    rows.firstOrNull { it.player.id == CURRENT_PLAYER_ID }?.let { me ->
                        binding.tvCurrentName.text = me.player.username
                        binding.tvCurrentAvatar.text =
                            me.player.username.first().uppercaseChar().toString()
                        binding.tvCurrentRank.text = getString(R.string.rank_value, me.rank)
                        binding.tvCurrentScore.text = getString(
                            R.string.score_value,
                            scoreFormat.format(me.player.score)
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenVisible()
    }

    override fun onStop() {
        viewModel.onScreenHidden()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.rvLeaderboard.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val CURRENT_PLAYER_ID = 12
    }
}
