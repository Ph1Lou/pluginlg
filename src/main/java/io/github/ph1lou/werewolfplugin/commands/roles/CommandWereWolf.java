package io.github.ph1lou.werewolfplugin.commands.roles;

import io.github.ph1lou.werewolfapi.Commands;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.enumlg.StatePlayer;
import io.github.ph1lou.werewolfapi.enumlg.TimersBase;
import io.github.ph1lou.werewolfapi.events.AppearInWereWolfListEvent;
import io.github.ph1lou.werewolfapi.events.RequestSeeWereWolfListEvent;
import io.github.ph1lou.werewolfplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandWereWolf implements Commands {


    private final Main main;

    public CommandWereWolf(Main main) {
        this.main = main;
    }

    @Override
    public void execute(Player player, String[] args) {

        WereWolfAPI game = main.getWereWolfAPI();
        UUID uuid = player.getUniqueId();

        RequestSeeWereWolfListEvent requestSeeWereWolfListEvent = new RequestSeeWereWolfListEvent(uuid);
        Bukkit.getPluginManager().callEvent(requestSeeWereWolfListEvent);


        if (!requestSeeWereWolfListEvent.isAccept()) {
            player.sendMessage(game.translate("werewolf.role.werewolf.not_werewolf"));
            return;
        }

        if (game.getConfig().getTimerValues().get(TimersBase.WEREWOLF_LIST.getKey()) > 0) {
            player.sendMessage(game.translate("werewolf.role.werewolf.list_not_revealed"));
            return;
        }

        StringBuilder list = new StringBuilder();

        for (UUID playerUUID : game.getPlayersWW().keySet()) {

            PlayerWW lg = game.getPlayersWW().get(playerUUID);
            AppearInWereWolfListEvent appearInWereWolfListEvent = new AppearInWereWolfListEvent(playerUUID);
            Bukkit.getPluginManager().callEvent(appearInWereWolfListEvent);

            if (lg.isState(StatePlayer.ALIVE) && appearInWereWolfListEvent.isAppear()) {
                list.append(lg.getName()).append(" ");
            }
        }
        player.sendMessage(game.translate("werewolf.role.werewolf.werewolf_list", list.toString()));
    }
}
