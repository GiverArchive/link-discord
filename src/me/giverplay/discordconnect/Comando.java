package me.giverplay.discordconnect;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.SecureRandom;

public class Comando implements CommandExecutor
{
  private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private DiscordConnect main;

  public Comando(DiscordConnect plugin)
  {
    main = plugin;
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
  {
    if(!DiscordConnect.getDatabaseEnabled())
    {
      commandSender.sendMessage(DiscordConnect.getPrefix() + "O banco de dados ainda não foi configurado... Comando desativado");
      return true;
    }

    if(!(commandSender instanceof Player))
    {
      commandSender.sendMessage(main.getPrefix() + "&cComando disponível apenas para jogadores");
      return true;
    }

    Player player = (Player) commandSender;
    Document playerDoc = new Document("nick", player.getName());
    Document findAsso = (Document) main.getAssociados().find(playerDoc).first();

    if(findAsso != null)
    {
      main.sendPlayer(player, "&cVocê já está associado com o Discord!");
      return true;
    }

    Document findAn = (Document) main.getAnalise().find(playerDoc).first();

    if(findAn != null)
    {
      main.sendPlayer(player, "&cVocê já fez um pedido de associação, agora basta usar seu código no Discord ou esperar mais alguns segundos antes de pedir um novo código");
      return true;
    }

    String token = generateToken();

    Document req = new Document("nick", player.getName());
    req.append("code", token);
    req.append("servidor", DiscordConnect.getServidor());

    main.getAnalise().insertOne(req);

    main.sendPlayer(player, main.getConfig().getString("servidor.sucesso"));
    main.sendPlayer(player,"&esua token: &f" + token);

    final String name = player.getName();

    new BukkitRunnable()
    {
      @Override
      public void run()
      {
        Document playerDocN = new Document("nick", name);
        Document findAssoN = (Document) main.getAnalise().find(playerDocN).first();

        if(findAssoN == null)
        {
          return;
        }

        main.getAnalise().deleteOne(findAssoN);
      }
    }.runTaskLaterAsynchronously(main, 20 * 300);

    return true;
  }

  private String generateToken()
  {
    String token = "";

    for(int i = 0; i < 6; i++)
    {
      token += String.valueOf(ALFABETO.charAt(new SecureRandom().nextInt(ALFABETO.length())));
    }

    return  token.trim();
  }
}
