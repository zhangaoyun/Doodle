package me.aoyun.doodle

object Dispatcher {

    fun start(request: Request) {
        val bitmap = MemoryCache.getBitmap(request.key)
        if (bitmap == null) {

        }
    }
}