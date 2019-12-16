package maxeem.america.gdg.repo

import maxeem.america.gdg.domain.GdgData
import maxeem.america.gdg.domain.LatLong
import org.jetbrains.anko.AnkoLogger

interface GdgRepository : AnkoLogger {

    suspend fun getData(region: String?, location: LatLong?) : GdgData

}
