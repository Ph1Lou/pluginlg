package io.github.ph1lou.pluginlg.commandlg.roles;

import io.github.ph1lou.pluginlg.MainLG;
import io.github.ph1lou.pluginlg.PlayerLG;
import io.github.ph1lou.pluginlg.commandlg.Commands;
import io.github.ph1lou.pluginlg.enumlg.RoleLG;
import io.github.ph1lou.pluginlg.enumlg.State;
import io.github.ph1lou.pluginlg.enumlg.StateLG;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandWildChild extends Commands {

    final MainLG main;

    public CommandWildChild(MainLG main, String name) {
        super(name);
        this.main=main;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)){
            return;
        }

        Player player =(Player) sender;
        String playername = player.getName();

        if(!main.playerLG.containsKey(playername)) {
            player.sendMessage(main.text.getText(67));
            return;
        }

        PlayerLG plg = main.playerLG.get(playername);

        if(!main.isState(StateLG.LG)) {
            player.sendMessage(main.text.getText(68));
            return;
        }

        if (!plg.isRole(RoleLG.ENFANT_SAUVAGE)){
            player.sendMessage(String.format(main.text.getText(189),main.text.translateRole.get(RoleLG.ENFANT_SAUVAGE)));
            return;
        }

        if (args.length!=1) {
            player.sendMessage(main.text.getText(54));
            return;
        }

        if(!plg.isState(State.LIVING)){
            player.sendMessage(main.text.getText(97));
            return;
        }

        if(!plg.hasPower()) {
            player.sendMessage(main.text.getText(103));
            return;
        }

        if(args[0].equals(playername)) {
            player.sendMessage(main.text.getText(105));
            return;
        }

        if(Bukkit.getPlayer(args[0])==null || !main.playerLG.containsKey(args[0]) || main.playerLG.get(args[0]).isState(State.MORT)) {
            player.sendMessage(main.text.getText(106));
            return;
        }

        plg.addAffectedPlayer(args[0]);
        plg.setPower(false);
        main.playerLG.get(args[0]).addDisciple(playername);
        player.sendMessage(String.format(main.text.powerHasBeenUse.get(RoleLG.ENFANT_SAUVAGE),args[0]));
    }
}