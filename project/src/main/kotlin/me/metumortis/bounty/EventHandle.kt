package me.metumortis.bounty

import me.metumortis.bounty.Bounty.Companion.publicConnection
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.plugin.java.JavaPlugin

class EventHandle(val plugin: JavaPlugin) : Listener {
     @EventHandler
     fun onDeathEvent(event: EntityDamageByEntityEvent){
        if(event.entity is Player && event.damager is Player){
            val player = event.entity as Player
            if(!player.isDead) return
            val killer = event.damager as Player
            val statement = publicConnection.createStatement()
            Bukkit.getServer().broadcastMessage("Öldürme gerçekleşti")
            val rs = statement.executeQuery("SELECT owns FROM players WHERE username = '${player.name}'")
            if(rs.next()){
                Bukkit.getServer().broadcastMessage("Bounty lisesi alındı")
                val bounties = rs.getString("owns").split(",")
                for(bountyID in bounties){
                    val bounty = statement.executeQuery("SELECT amount,type FROM bounties WHERE id = $bountyID and claimed = 0")
                    if(bounty.next()){
                        Bukkit.getServer().broadcastMessage("Bountyler işleme alındı")
                        val amount = bounty.getInt("amount")
                        val type = bounty.getString("type")
                        val console = Bukkit.getConsoleSender()
                        if(type.equals("MONEY")){
                            Bukkit.getServer().broadcastMessage("Para verildi")
                            Bukkit.dispatchCommand(console, "eco give ${killer.name} $amount")
                        }
                        else if(type.equals("HEART")){
                            Bukkit.getServer().broadcastMessage("Kalp verildi")
                            Bukkit.dispatchCommand(console, "lifesteal add ${killer.name} $amount")
                        }
                    }
                }
                statement.executeUpdate("UPDATE bounties SET claimed = 1 WHERE forWho = '${player.name}'")
            }
        }
     }
}