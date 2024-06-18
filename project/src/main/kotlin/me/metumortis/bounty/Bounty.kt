package me.metumortis.bounty
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection

class Bounty : JavaPlugin() {

    override fun onEnable(){
        if(!this.dataFolder.exists()) this.dataFolder.mkdir()
        this.saveDefaultConfig()
        this.reloadConfig()
        publicConfig = config;
        val connection = Data(this.dataFolder.absolutePath+"/data.sqlite")
        println(this.dataFolder.absolutePath+"/data.sqlite")
        val db = connection.connect()
        publicConnection = db;
        val statement = db.createStatement()
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS bounties (id INTEGER PRIMARY KEY AUTOINCREMENT , type TEXT, amount INT, forWho TEXT, fromWho TEXT, claimed INT)")
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (username TEXT, owns TEXT, giving TEXT)")
        getCommand("bounty")!!.setExecutor(CommandHandle(this))
        server.pluginManager.registerEvents(EventHandle(this), this)


        //val getCurrentBounty = db.prepareStatement("SELECT amount,type FROM bounties WHERE forWho = (?)")
        //getCurrentBounty.setString(1, "Shqsly")
        //val result = getCurrentBounty.executeQuery()
        //while(result.next()){
        //    println("Type: ${result.getString("type")}, Amount: ${result.getInt("amount")}")
        //}
        //statement.executeUpdate("CREATE TABLE IF NOT EXISTS person (id integer, name string)")
        //statement.executeUpdate("insert into person values(1, 'leo')");
        //statement.executeUpdate("insert into person values(2, 'yui')");


    }

    override fun onDisable() {
        publicConnection.close();
    }

    companion object{
        lateinit var publicConnection: Connection;
        lateinit var publicConfig: FileConfiguration;
        fun translateColors(str: String): String {
            if("&#[0-9a-f]{6}".toRegex().containsMatchIn(str)){
                var parsedStr = str
                //println(str.replace("&#([0-9a-f]{6})".toRegex(), "\u00A7$1"));
                for (x in "&(#[0-9A-f]{6})".toRegex().findAll(str)){
                    parsedStr = parsedStr.replaceFirst(x.value.toRegex(), net.md_5.bungee.api.ChatColor.of(x.value.slice(
                        1 until x.value.length
                    )).toString())
                }

                return ChatColor.translateAlternateColorCodes('&', parsedStr)
            }
            return ChatColor.translateAlternateColorCodes('&', str)
        }
    }
}
fun main(){
    /*
    val connection = Data("/home/metumortis/Desktop/Sunucu Kurma Klasörü/plugins/Bounty/data.sqlite")
    val db = connection.connect()
    val statement = db.createStatement()
    statement.executeUpdate("CREATE TABLE IF NOT EXISTS bounties (id INTEGER PRIMARY KEY AUTOINCREMENT , type TEXT, amount INT, forWho TEXT, fromWho TEXT, claimed INT)")
    statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (username TEXT, owns TEXT, giving TEXT)")
    //val rs = statement.executeQuery("SELECT amount,type FROM bounties WHERE id=2")
    //val rs = statement.executeQuery("SELECT id FROM bounties ORDER BY DESC id")
    //val rs = statement.executeQuery("Select COUNT(*) FROM bounties")
    //statement.executeUpdate("INSERT INTO bounties (id, type, amount, forWho, fromWho, claimed) VALUES (25, 'HEART', 15, 'MetuMortis', '(CONSOLE)', 0)")
    //println(rs.getInt("COUNT(*)"))
    val playerData = statement.executeQuery("SELECT giving FROM players WHERE username='(CONSOLE)'")
    val targetData = statement.executeQuery("SELECT owns FROM players WHERE username='MetuMortis'")
    //println(playerData.getString("giving"))
    println(targetData.getString("owns"))
    val size = statement.executeQuery("Select COUNT(*) FROM players WHERE username='MetuMortis'")
    println(size.getString("COUNT(*)"))

     */

}

