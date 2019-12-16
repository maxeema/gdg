package maxeem.america.gdg.ui.adapter

import kotlinx.coroutines.*
import maxeem.america.gdg.domain.GdgChapter
import maxeem.america.glob.thread
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class AdapterFilter(val query: String, private inline val onFiltered: AdapterFilter.(List<GdgChapter>)->Unit)
    : AnkoLogger {
    var job : Job? = null
        private set

    fun isSame(newQuery: String?) = query == newQuery

    fun go(list: List<GdgChapter>, scope: CoroutineScope) {
        job = scope.async {
            info("- filter go, on: $query, job: $job, thread: $thread")
            val filtered =
                withContext(Dispatchers.Default) {
                    list.filter {
                        with(it) {
                            name.contains(query, ignoreCase = true) || website.contains(query, true)
                                || country.contains(query, true) || region.contains(query, true)
                                    || city.contains(query, true)
                        }
                    }
                }
            info("- filter, isActive: $isActive, result size: ${filtered.size}, $this")
            if (isActive)
                this@AdapterFilter.onFiltered(filtered)
        }.apply {
            invokeOnCompletion {
                info("- filter invokeOnCompletion, isCanceled: $isCancelled, $this")
            }
        }
        info("- filter, started: $job")
    }

    fun cancel() {
        job?.cancel()
    }
}