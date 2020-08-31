package io.github.ph1lou.werewolfplugin.listeners;

import io.github.ph1lou.werewolfapi.ScoreAPI;
import io.github.ph1lou.werewolfapi.enumlg.Day;
import io.github.ph1lou.werewolfapi.enumlg.Sounds;
import io.github.ph1lou.werewolfapi.enumlg.StateLG;
import io.github.ph1lou.werewolfapi.enumlg.VoteStatus;
import io.github.ph1lou.werewolfapi.events.*;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import io.github.ph1lou.werewolfplugin.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CycleListener implements Listener {

    private final Main main;
    private final GameManager game;

    public CycleListener(Main main, GameManager game) {
        this.main = main;
        this.game = game;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDay(DayEvent event) {

        game.setDay(Day.DAY);

        if(game.isState(StateLG.END)) return;

        long duration = game.getConfig().getTimerValues().get("werewolf.menu.timers.vote_duration");
        Bukkit.broadcastMessage(game.translate("werewolf.announcement.day", event.getNumber()));
        groupSizeChange();

        if (game.getConfig().getConfigValues().get("werewolf.menu.global.vote") && game.getScore().getPlayerSize() < game.getConfig().getPlayerRequiredVoteEnd()) {
            game.getConfig().getConfigValues().put("werewolf.menu.global.vote", false);
            Bukkit.broadcastMessage(game.translate("werewolf.vote.vote_deactivate"));
            game.getVote().setStatus(VoteStatus.ENDED);
        }

        if(2*game.getConfig().getTimerValues().get("werewolf.menu.timers.day_duration") - duration-game.getConfig().getTimerValues().get("werewolf.menu.timers.citizen_duration")>0){

            if (game.getConfig().getConfigValues().get("werewolf.menu.global.vote") && game.getConfig().getTimerValues().get("werewolf.menu.timers.vote_begin") < 0) {
                Bukkit.broadcastMessage(game.translate("werewolf.vote.vote_time", game.getScore().conversion((int) duration)));
                game.getVote().setStatus(VoteStatus.IN_PROGRESS);
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    if(!game.isState(StateLG.END)){
                        Bukkit.getPluginManager().callEvent(new VoteEndEvent());
                    }

                },duration*20);
            }
        }
        long duration2 = game.getConfig().getTimerValues().get("werewolf.menu.timers.power_duration");

        if (2*game.getConfig().getTimerValues().get("werewolf.menu.timers.day_duration")-duration2>0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

                if(!game.isState(StateLG.END)){
                    Bukkit.getPluginManager().callEvent(new SelectionEndEvent());
                }
            },duration2*20);

        }

        long duration3 = game.getConfig().getTimerValues().get("werewolf.menu.timers.day_duration");

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if(!game.isState(StateLG.END)){
                Bukkit.getPluginManager().callEvent(new NightEvent( event.getNumber()));
            }

        },duration3*20);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onNight(NightEvent event){


        long duration  =game.getConfig().getTimerValues().get("werewolf.menu.timers.day_duration")-30;
        game.setDay(Day.NIGHT);

        if(game.isState(StateLG.END)) return;

        Bukkit.broadcastMessage(game.translate("werewolf.announcement.night", event.getNumber()));
        groupSizeChange();

        if(duration>0){
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                if(!game.isState(StateLG.END)){
                    Bukkit.getPluginManager().callEvent(new DayWillComeEvent());
                }

            },duration*20);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if(!game.isState(StateLG.END)){
                Bukkit.getPluginManager().callEvent(new DayEvent(event.getNumber()+1));
            }

        },(duration+30)*20);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVoteEnd(VoteEndEvent event) {

        long duration = game.getConfig().getTimerValues().get("werewolf.menu.timers.citizen_duration");
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if (!game.isState(StateLG.END)) {
                Bukkit.getPluginManager().callEvent(new VoteResultEvent());
            }

        }, duration * 20);
    }

    public void groupSizeChange() {

        ScoreAPI score = game.getScore();

        if (score.getPlayerSize() <= score.getGroup() * 3 && score.getGroup() > 3) {
            score.setGroup(score.getGroup() - 1);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(game.translate("werewolf.commands.admin.group.group_change", score.getGroup()));
                VersionUtils.getVersionUtils().sendTitle(p, game.translate("werewolf.commands.admin.group.top_title"), game.translate("werewolf.commands.admin.group.bot_title", score.getGroup()), 20, 60, 20);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWereWolfList(WereWolfListEvent event) {
        game.updateNameTag();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoverRepartition(LoversRepartitionEvent event) {
        game.getLoversManage().autoLovers();
    }

    @EventHandler
    public void onPVP(PVPEvent event) {

        game.getWorld().setPVP(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(game.translate("werewolf.announcement.pvp"));
            Sounds.DONKEY_ANGRY.play(p);
        }
    }

    @EventHandler
    public void onDiggingEnd(DiggingEndEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(game.translate("werewolf.announcement.mining"));
            Sounds.ANVIL_BREAK.play(p);
        }
    }

    @EventHandler
    public void onTrollSV(TrollEvent event) {

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (game.getPlayersWW().containsKey(p.getUniqueId())) {
                p.sendMessage(game.translate("werewolf.role.villager.description"));
                Sounds.EXPLODE.play(p);
            }
        }


        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (game.getConfig().isTrollSV() && game.getPlayersWW().containsKey(p.getUniqueId())) {
                    Sounds.PORTAL_TRIGGER.play(p);
                    p.sendMessage(game.translate("werewolf.announcement.troll"));
                }
            }
            game.getConfig().setTrollSV(false);
            game.getRoleManage().repartitionRolesLG();

        }, 1800L);
    }
}