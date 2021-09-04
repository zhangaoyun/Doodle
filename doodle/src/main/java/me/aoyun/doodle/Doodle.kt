package me.aoyun.doodle

object Doodle {

    fun config(): Config {
        return Config
    }

    fun load(path: String): Request {
        return Request(path)
    }
}