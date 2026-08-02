package com.battlebucks.app.presentation.leaderboard.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.battlebucks.app.R
import com.battlebucks.app.databinding.ItemLeaderboardBinding
import com.battlebucks.app.domain.model.RankedPlayer
import java.text.NumberFormat
import java.util.Locale

class LeaderboardAdapter(
    private val currentPlayerId: Int
) : ListAdapter<RankedPlayer, LeaderboardAdapter.ViewHolder>(DiffCallback) {

    private val scoreFormat = NumberFormat.getIntegerInstance(Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bind(getItem(position))
        } else {
            val oldScore = payloads.last() as? Long
            holder.bind(getItem(position), oldScore)
        }
    }

    inner class ViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var scoreAnimator: ValueAnimator? = null

        fun bind(item: RankedPlayer, oldScore: Long? = null) {
            val player = item.player
            val isMe = player.id == currentPlayerId
            val context = binding.root.context

            binding.tvRank.text = "#${item.rank}"
            binding.tvAvatar.text = player.username.first().uppercaseChar().toString()
            binding.tvUsername.text = player.username
            binding.tvMovement.text = "—"
            binding.tvMovement.setTextColor(
                ContextCompat.getColor(context, R.color.secondary_text)
            )

            // Orange border for logged-in user
            if (isMe) {
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.current_player_surface)
                )
                binding.root.strokeColor =
                    ContextCompat.getColor(context, R.color.primary_orange)
                binding.root.strokeWidth = (2 * context.resources.displayMetrics.density).toInt()
            } else {
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.card_surface)
                )
                binding.root.strokeColor =
                    ContextCompat.getColor(context, R.color.card_stroke)
                binding.root.strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
            }

            if (oldScore != null && oldScore != player.score) {
                animateScore(oldScore, player.score)
            } else {
                binding.tvScore.text = context.getString(
                    R.string.score_points,
                    scoreFormat.format(player.score)
                )
            }

            if (item.isHighlighted) {
                highlightRow()
            }
        }

        private fun animateScore(from: Long, to: Long) {
            scoreAnimator?.cancel()
            scoreAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300L
                addUpdateListener { anim ->
                    val value = from + ((to - from) * anim.animatedFraction).toLong()
                    binding.tvScore.text = binding.root.context.getString(
                        R.string.score_points,
                        scoreFormat.format(value)
                    )
                }
                start()
            }

            binding.tvScore.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(150L)
                .withEndAction {
                    binding.tvScore.animate().scaleX(1f).scaleY(1f).setDuration(150L).start()
                }
                .start()
        }

        private fun highlightRow() {
            val context = binding.root.context
            val base = ContextCompat.getColor(context, R.color.card_surface)
            val highlight = ContextCompat.getColor(context, R.color.card_highlight)

            ValueAnimator.ofArgb(base, highlight, base).apply {
                duration = 600L
                addUpdateListener { anim ->
                    binding.root.setCardBackgroundColor(anim.animatedValue as Int)
                }
                start()
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RankedPlayer>() {
        override fun areItemsTheSame(oldItem: RankedPlayer, newItem: RankedPlayer) =
            oldItem.player.id == newItem.player.id

        override fun areContentsTheSame(oldItem: RankedPlayer, newItem: RankedPlayer) =
            oldItem == newItem

        override fun getChangePayload(oldItem: RankedPlayer, newItem: RankedPlayer): Any? {
            return if (oldItem.player.score != newItem.player.score) {
                oldItem.player.score
            } else {
                null
            }
        }
    }
}
