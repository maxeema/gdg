package maxeem.america.gdg.ui.adapter

import android.animation.ObjectAnimator
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import maxeem.america.gdg.databinding.LoadingItemBinding

class GdgLoadingAdapter : RecyclerView.Adapter<GdgLoadingViewHolder>() {

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GdgLoadingViewHolder {
        return GdgLoadingViewHolder.from(
            parent
        )
    }

    override fun onBindViewHolder(holder: GdgLoadingViewHolder, position: Int) {
        holder.animate()
    }

}

class GdgLoadingViewHolder(private var binding: LoadingItemBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {

        private const val FADE_DURATION = 1000L

        fun from(parent: ViewGroup) =
            GdgLoadingViewHolder(
                LoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
    }

    private val animation = ObjectAnimator.ofFloat(itemView, View.ALPHA, 1f, 0f, 1f).apply {
        repeatCount = ObjectAnimator.INFINITE
        duration =
            FADE_DURATION
        // Reset the alpha on animation end.
        doOnEnd { itemView.alpha = 1f }
    }

    fun animate() {
        // Shift the timing of fade-in/out for each item by its adapter position. We use the
        // elapsed real time to make this independent from the timing of method call.
        animation.currentPlayTime =
            (SystemClock.elapsedRealtime() - adapterPosition * 30L) % FADE_DURATION
        animation.start()
    }

}
