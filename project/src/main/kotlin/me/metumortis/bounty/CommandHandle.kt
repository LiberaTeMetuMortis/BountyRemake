package me.metumortis.bounty

import me.clip.placeholderapi.PlaceholderAPI
import me.metumortis.bounty.Bounty.Companion.publicConfig
import me.metumortis.bounty.Bounty.Companion.publicConnection
import me.metumortis.bounty.Bounty.Companion.translateColors
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class CommandHandle(val plugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args[0] == "add"){
            // /bounty add MetuMortis heart 10
            if(args.size != 4){
                sender.sendMessage(publicConfig.getString("usages.add")?.let(::translateColors))
                return true
            }
            if(!args[2].matches(Regex("(?:HEART|MONEY)S?", RegexOption.IGNORE_CASE))){
                sender.sendMessage(publicConfig.getString("messages.invalidType")?.let(::translateColors))
                return true
            }
            if(args[3].toIntOrNull() == null){
                sender.sendMessage(publicConfig.getString("messages.NaN")?.let(::translateColors))
                return true
            }
            if(sender is Player){
                val moneyOfPlayer = PlaceholderAPI.setPlaceholders(sender, "%vault_eco_balance%").toDoubleOrNull()
                val heartsOfPlayer = PlaceholderAPI.setPlaceholders(sender, "%lifesteal_hearts%").toIntOrNull()
                val target = args[1]
                val type = args[2]
                val amount = args[3]
                if(type.matches(Regex("HEART(S?)")) && heartsOfPlayer != null){
                    if(amount.toInt() > heartsOfPlayer){
                        sender.sendMessage(publicConfig.getString("notEnoughHearts")?.let(::translateColors))
                        return true
                    }
                    val statement = publicConnection.createStatement()
                    val rs = statement.executeQuery("SELECT COUNT(*) FROM bounties")
                    val id = rs.getInt("COUNT(*)")+1
                    saveToDatabase(target, sender.name, id, "HEART", amount.toInt())
                    val console = Bukkit.getConsoleSender()
                    Bukkit.dispatchCommand(console, "lifesteal remove ${sender.name} $amount")
                    sender.sendMessage(publicConfig.getString("messages.putBounty")?.let{
                        it.replace("{amount}", amount)
                          .replace("{type}", "HEART")
                          .replace("{player}", target)
                    }?.let(::translateColors))


                }
                else if(type.equals("MONEY", ignoreCase = true) && moneyOfPlayer != null){
                    if(amount.toInt() > moneyOfPlayer){
                        sender.sendMessage(publicConfig.getString("notEnoughMoney")?.let(::translateColors))
                        return true
                    }
                    val statement = publicConnection.createStatement()
                    val rs = statement.executeQuery("SELECT COUNT(*) FROM bounties")
                    val id = rs.getInt("COUNT(*)")+1
                    saveToDatabase(target, sender.name, id, "MONEY", amount.toInt())
                    val console = Bukkit.getConsoleSender()
                    Bukkit.dispatchCommand(console, "eco take ${sender.name} $amount")
                    sender.sendMessage(publicConfig.getString("messages.putBounty")?.let{
                        it.replace("{amount}", amount)
                          .replace("{type}", "MONEY")
                          .replace("{player}", target)
                    }?.let(::translateColors))
                }
            }
            else{
                val target = args[1]
                val type = args[2]
                val amount = args[3]
                val statement = publicConnection.createStatement()
                val rs = statement.executeQuery("SELECT COUNT(*) FROM bounties")
                val id = rs.getInt("COUNT(*)")+1
                if(type.matches(Regex("HEART(S?)", RegexOption.IGNORE_CASE))) {
                    saveToDatabase(target, "(CONSOLE)", id, "HEART", amount.toInt())
                    sender.sendMessage(
                        publicConfig.getString("messages.putBounty")!!
                            .replace("{amount}", amount)
                            .replace("{type}", "HEART")
                            .replace("{player}", target)
                            .let(::translateColors)
                    )
                }
                else if(type.equals("MONEY", ignoreCase = true)){
                    saveToDatabase(target, "(CONSOLE)", id, "MONEY", amount.toInt())
                    sender.sendMessage(
                        publicConfig.getString("messages.putBounty")!!
                            .replace("{amount}", amount)
                            .replace("{type}", "MONEY")
                            .replace("{player}", target)
                            .let(::translateColors)
                    )
                }
            }
        }
        else if(args[0] == "remove"){
            // /bounty remove MetuMortis
            if(args.size != 2){
                sender.sendMessage(publicConfig.getString("usages.remove")?.let(::translateColors))
                return true
            }
            if(sender is Player){
                val target = args[1]
                val statement = publicConnection.createStatement()
                val rs = statement.executeQuery("SELECT * FROM bounties WHERE forWho = '$target' AND fromWho = '${sender.name}' AND claimed = 0")
                if(rs.next()){
                    val id = rs.getInt("id")
                    val type = rs.getString("type")
                    val amount = rs.getInt("amount")
                    if(type.equals("HEART", ignoreCase = true)){
                        val console = Bukkit.getConsoleSender()
                        Bukkit.dispatchCommand(console, "lifesteal add ${sender.name} $amount")
                    }
                    else if(type.equals("MONEY", ignoreCase = true)){
                        val console = Bukkit.getConsoleSender()
                        Bukkit.dispatchCommand(console, "eco give ${sender.name} $amount")
                    }
                    statement.executeUpdate("UPDATE bounties SET claimed=1 WHERE forWho='$target' AND fromWho='${sender.name}'")
                }
                else{
                    sender.sendMessage(publicConfig.getString("messages.noBounty"))
                }
            }
            else{
                val target = args[1]
                val statement = publicConnection.createStatement()
                val rs = statement.executeQuery("SELECT * FROM bounties WHERE forWho = '$target' AND fromWho = '(CONSOLE)' AND claimed = 0")
                if(rs.next()){
                    statement.executeUpdate("UPDATE bounties SET claimed=1 WHERE forWho='$target' AND fromWho='(CONSOLE)'")
                }
                else{
                    sender.sendMessage(publicConfig.getString("messages.noBounty")?.let(::translateColors))
                }
            }
        }

        return true;
    }
}

fun saveToDatabase(target: String, player: String, id: Int, type: String, amount: Int){

    val prepared = publicConnection.prepareStatement("INSERT INTO bounties (id, type, amount, forWho, fromWho, claimed) VALUES (?, ?, ?, ?, ?, 0)")
    prepared.setInt(1, id)
    prepared.setString(2, type)
    prepared.setInt(3, amount)
    prepared.setString(4, target)
    prepared.setString(5, player)
    prepared.executeUpdate()
    var statement = publicConnection.createStatement()
    val targetSize = statement.executeQuery("SELECT COUNT(*) FROM players WHERE username='$target'")
    val playerSize = statement.executeQuery("SELECT COUNT(*) FROM players WHERE username='$player'")
    println("TargetSize: $targetSize and PlayerSize: $playerSize")
    statement = publicConnection.createStatement()
    if(targetSize.getInt("COUNT(*)") == 0){
        statement.executeUpdate("INSERT INTO players (username, owns, giving) VALUES ('$target', '$id', '')")
    }
    else{
        val targetOwns = statement.executeQuery("SELECT owns FROM players WHERE username='$target'").getString("owns")
        val args = targetOwns.split(",").toMutableSet()
        args.add(id.toString())
        statement.executeUpdate("UPDATE players SET owns='${args.joinToString(separator = ",")}' WHERE username='$target'")
    }
    if(playerSize.getInt("COUNT(*)") == 0){
        statement.executeUpdate("INSERT INTO players (username, owns, giving) VALUES ('$player', '', '$id')")
    }
    else{
        val playerGiving = statement.executeQuery("SELECT giving FROM players WHERE username='$player'").getString("giving")
        val args = playerGiving.split(",").toMutableSet()
        args.add(id.toString())
        statement.executeUpdate("UPDATE players SET giving='${args.joinToString(separator = ",")}' WHERE username='$player'")
    }
}