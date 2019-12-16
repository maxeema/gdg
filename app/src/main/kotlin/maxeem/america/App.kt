package maxeem.america

import android.app.Application
import maxeem.america.gdg.net.GdgApi
import maxeem.america.gdg.repo.GdgRepository
import maxeem.america.gdg.repo.GdgRepositoryImpl
import maxeem.america.ext.hash
import maxeem.america.glob.pid
import maxeem.america.glob.timeMillis
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

val app = App.instance

private val appModule = module {
    single { GdgApi.service }
    single<GdgRepository> { GdgRepositoryImpl() }
}

class App : Application(), AnkoLogger {

    companion object {
        lateinit var instance: App
            private set
    }

    init {
        info("$pid - $hash $timeMillis init")
        instance = this
    }

    override fun onCreate() { super.onCreate()
        startKoin {
            androidLogger(); androidContext(this@App)
            modules(appModule)
        }
    }

}
