package io.github.ph1lou.werewolfplugin.listeners;


import io.github.ph1lou.werewolfapi.enumlg.Sounds;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import io.github.ph1lou.werewolfplugin.game.ModerationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final GameManager game;
    private final Main main;

    public ChatListener(Main main, GameManager game) {
        this.main = main;
        this.game = game;
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        ModerationManager moderationManager = game.getModerationManager();

        if (args[0].equalsIgnoreCase("/rl") || args[0].equalsIgnoreCase("/reload") || args[0].equalsIgnoreCase("/bukkit:rl") || args[0].equalsIgnoreCase("/bukkit:reload")) {
            event.setCancelled(true);
            player.sendMessage(game.translate("werewolf.check.disabled_command"));
        } else if (args[0].equalsIgnoreCase("/me") || args[0].equalsIgnoreCase("/minecraft:me")) {
            event.setCancelled(true);
            player.sendMessage(game.translate("werewolf.check.disabled_command"));
        } else if (args[0].equalsIgnoreCase("/tellRaw") || args[0].equalsIgnoreCase("/msg") || args[0].equalsIgnoreCase("/tell") || args[0].equalsIgnoreCase("/minecraft:tell")) {

            event.setCancelled(true);
            if (args.length <= 2) return;

            Player recipient = Bukkit.getPlayer(args[1]);

            if (recipient == null) {
                player.sendMessage(game.
                        translate("werewolf.check.offline_player"));
                return;
            }

            if (!recipient.hasPermission("a.use") && !player.hasPermission("tell.use")) {

                if (!moderationManager.getHosts().contains(recipient.getUniqueId()) && !moderationManager.getHosts().contains(player.getUniqueId())) {
                    if (!moderationManager.getModerators().contains(recipient.getUniqueId()) && !moderationManager.getModerators().contains(player.getUniqueId())) {
                        player.sendMessage(game.translate("werewolf.check.permission_denied"));
                        return;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            for (String w : args) {
                sb.append(w).append(" ");
            }
            sb.delete(0, args[0].length() + args[1].length() + 2);
            recipient.sendMessage(game.translate("werewolf.commands.message.received", player.getName(), sb.toString()));
            player.sendMessage(game.translate("werewolf.commands.message.send", args[1], sb.toString()));
            Sounds.ANVIL_USE.play(recipient);
        }


    }

    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ModerationManager moderationManager = game.getModerationManager();
        String format;

        if(player.getName().equals("Ph1Lou")){
            format=game.translate("werewolf.commands.admin.chat.template","§5✦§r %s","%s");
        }
        else format=game.translate("werewolf.commands.admin.chat.template","%s","%s");

        if (moderationManager.getHosts().contains(uuid)) {
            event.setFormat(game.translate("werewolf.commands.admin.host.tag") + format);
        } else if (moderationManager.getModerators().contains(uuid)) {
            event.setFormat(game.translate("werewolf.commands.admin.moderator.tag") + format);
        } else event.setFormat(format);

        if (!game.getConfig().getConfigValues().get("werewolf.menu.global.chat")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(game.translate("werewolf.commands.admin.chat.off"));
            if (main.getConfig().getBoolean("bungeechat")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chatlock local");
            }
        }
        else if(game.getConfig().getConfigValues().get("werewolf.menu.global.proximity_chat")){

            event.setCancelled(true);

            for(Player p:Bukkit.getOnlinePlayers()){
                double distance =p.getLocation().distance(player.getLocation());

                if(distance<20){
                    p.sendMessage(String.format(event.getFormat(),player.getName(),event.getMessage()));
                }
                else if (distance<=50){
                    p.sendMessage(String.format(event.getFormat(),player.getName(),obfuscation(event.getMessage(), ((int)distance-20)/70f)));
                }
            }

        }
    }

    private String obfuscation(String message, float percentage) {

        StringBuilder returnMessage = new StringBuilder();
        for(int i=0;i<message.length();i++){
            char charOfMessage =message.charAt(i);

            if(Math.random()<percentage){
                if(charOfMessage!=' '){
                    returnMessage.append("?");
                }
                else returnMessage.append(" ");
            }
            else returnMessage.append(charOfMessage);
        }

        return returnMessage.toString();
    }
}

