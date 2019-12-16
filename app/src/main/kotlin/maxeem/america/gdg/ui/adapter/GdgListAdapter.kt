package maxeem.america.gdg.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import maxeem.america.gdg.databinding.ListItemBinding
import maxeem.america.gdg.domain.GdgChapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class GdgListAdapter(private val onClick: (View)->Unit)
        : AnkoLogger, ListAdapter<GdgChapter, GdgListViewHolder>(
    DiffCallback
) {

    var list : List<GdgChapter>? = null
        private set

    var filter : AdapterFilter? = null
        private set

    private var scope: CoroutineScope? = null
    private var onFilter: ((List<GdgChapter>) -> Unit)? = null

    companion object DiffCallback : DiffUtil.ItemCallback<GdgChapter>() {
        override fun areItemsTheSame(oldItem: GdgChapter, newItem: GdgChapter)
            = oldItem.website == newItem.website
        override fun areContentsTheSame(oldItem: GdgChapter, newItem: GdgChapter)
            = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = GdgListViewHolder.from(parent)

    override fun onBindViewHolder(holder: GdgListViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    private fun submitListFiltered(filtered: List<GdgChapter>) {
        super.submitList(filtered) {
            onFilter?.invoke(filtered)
        }
    }
    override fun submitList(list: List<GdgChapter>?) { submitList(list, null) }
    override fun submitList(list: List<GdgChapter>?, commitCallback: Runnable?) {
        filter?.cancel()
        this.list = list
        super.submitList(list, commitCallback)
    }

    fun setFilterable(scope: CoroutineScope, onFilter: (List<GdgChapter>) -> Unit) {
        this.scope = scope
        this.onFilter = onFilter
    }
    fun filter(query: String) = true.apply exit@ {
        requireNotNull(scope)
        info("filter, new query: $query, last query: ${filter?.query}, last job: ${filter?.job}")
        if (filter?.isSame(query) == true) return@exit
        filter?.cancel()
        filter = AdapterFilter(query) { filtered ->
            submitListFiltered(filtered)
        }
        val list = list ?: return@exit
        if (query.isEmpty()) {
            submitListFiltered(list)
        } else {
            filter!!.go(list, scope!!)
        }
    }

}

class GdgListViewHolder(private var binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(gdgChapter: GdgChapter, onClick: (View)->Unit) = binding.apply {
        chapter = gdgChapter
        root.tag = chapter
        root.setOnClickListener(onClick)
        executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup) =
            GdgListViewHolder(
                ListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
    }

}