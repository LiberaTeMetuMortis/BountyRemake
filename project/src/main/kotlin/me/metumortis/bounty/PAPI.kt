package me.metumortis.bounty

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PAPI : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "Bounty.kt"
    }

    override fun getAuthor(): String {
        return "MetuMortis"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        TODO("Yapıcam")
    }

}