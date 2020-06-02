package me.giverplay.discordconnect;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DiscordConnect extends JavaPlugin
{
  private static String prefix = "&b[Discord]";
  private static String servidor = "def";
  private static boolean disable = false;

  private MongoClient client;
  private MongoDatabase mongodb;
  private MongoCollection associados;
  private MongoCollection analise;

  public static String getPrefix()
  {
    return prefix;
  }

  public static String getServidor()
  {
    return servidor;
  }

  public static boolean getDatabaseEnabled()
  {
    return !disable;
  }

  public void sendConsole(String toBroadcast)
  {
    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + "&r " + toBroadcast));
  }

  public void sendPlayer(Player player, String msg)
  {
    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + "&r " + msg));
  }

  public MongoDatabase getMongoDatabase()
  {
    return this.mongodb;
  }

  public MongoCollection getAssociados()
  {
    return this.associados;
  }

  public MongoClient getClient()
  {
    return client;
  }

  public MongoCollection getAnalise()
  {
    return analise;
  }

  @Override
  public void onEnable()
  {
    sendConsole("&aIniciando plugin.");

    File file = getDataFolder();

    if(!getDataFolder().exists())
    {
      disable = true;
      file.mkdir();
      saveDefaultConfig();
    }
    else
    {
      if(!getConfig().isSet("mongo")
              || !getConfig().isSet("mongo.link_acesso")
              || !getConfig().isSet("mongo.collection_dos_associados")
              || !getConfig().isSet("mongo.collection_dos_em_analise")
              || !getConfig().isSet("servidor.nome")
              || !getConfig().isSet("servidor.prefix")
              || !getConfig().isSet("servidor.sucesso")
              || !getConfig().isSet("mongo.database"))
      {
        disable = true;
      }

      String link = getConfig().getString("mongo.link_acesso");
      String database = getConfig().getString("mongo.database");
      String asso = getConfig().getString("mongo.collection_dos_associados");
      String analise = getConfig().getString("mongo.collection_dos_em_analise");
      servidor = getConfig().getString("servidor.nome");
      prefix = getConfig().getString("servidor.prefix");

      client = new MongoClient(new MongoClientURI(link));
      mongodb = client.getDatabase(database);

      if(database == null)
      {
        disable = true;
        sendConsole("Database incorreta");
      }

      associados = mongodb.getCollection(asso);

      if(associados == null)
      {
        disable = true;
        sendConsole("Collection de associados incorreta");
      }

      this.analise = mongodb.getCollection(analise);

      if(this.analise == null)
      {
        disable = true;
        sendConsole("Collection de pedido de associar incorreta");
      }
    }

    if(disable)
      sendConsole("&cO banco de dados não foi configurado corretamente, verifique o arquivo config.yml");

    getCommand("discord").setExecutor(new Comando(this));
  }

  @Override
  public void onDisable()
  {
    sendConsole("&cFinalizando plugin.");
    client.close();
  }
}
