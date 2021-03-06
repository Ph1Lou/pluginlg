package io.github.ph1lou.werewolfplugin.tasks;

import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.enumlg.Sounds;
import io.github.ph1lou.werewolfapi.enumlg.StateLG;
import io.github.ph1lou.werewolfapi.events.DayEvent;
import io.github.ph1lou.werewolfapi.events.UpdateEvent;
import io.github.ph1lou.werewolfapi.versions.VersionUtils;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TransportationTask extends BukkitRunnable {

    private final Main main;
    private final GameManager game;
    private int i = 0;
    private int j = 0;

    public TransportationTask(Main main, GameManager game) {
        this.main = main;
        this.game=game;
    }


    private void createStructure(Material m, Location location) {

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        World world = game.getMapManager().getWorld();
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                if (Math.abs(j) == 2 || Math.abs(i) == 2) {
                    for (int k = 0; k < 2; k++) {
                        new Location(world, x + i, y - 1 + k, z + j).getBlock().setType(m);
                    }
                }
                new Location(world, x + i, y - 2, z + j).getBlock().setType(m);
                new Location(world, x + i, y + 2, z + j).getBlock().setType(m);
            }
        }
    }

    @Override
    public void run() {


        if (game.isState(StateLG.END)) {
            cancel();
            return;
        }
        Bukkit.getPluginManager().callEvent(new UpdateEvent());
        World world = game.getMapManager().getWorld();
        WorldBorder wb = world.getWorldBorder();

        if (i < game.getPlayersWW().size()) {

            for (Player p : Bukkit.getOnlinePlayers()) {
                Sounds.DIG_GRASS.play(p);
                VersionUtils.getVersionUtils().sendActionBar(p, game.translate("werewolf.action_bar.create_tp_point", i + 1, game.getPlayersWW().size()));
            }

            UUID uuid = (UUID) game.getPlayersWW().keySet().toArray()[i];

            double a = i * 2 * Math.PI / Bukkit.getOnlinePlayers().size();
            int x = (int) (Math.round(wb.getSize() / 3 * Math.cos(a) + world.getSpawnLocation().getX()));
            int z = (int) (Math.round(wb.getSize() / 3 * Math.sin(a) + world.getSpawnLocation().getZ()));
            Location spawn = new Location(world, x, world.getHighestBlockYAt(x, z) + 100, z);
            world.getChunkAt(x, z).load(true);
            createStructure(Material.BARRIER, spawn);
            game.getPlayersWW().get(uuid).setSpawn(spawn.clone());
        } else if (i < 2 * game.getPlayersWW().size()) {

            for (Player p : Bukkit.getOnlinePlayers()) {
                Sounds.ORB_PICKUP.play(p);
                VersionUtils.getVersionUtils().sendActionBar(p, game.translate("werewolf.action_bar.tp", i - game.getPlayersWW().size() + 1, game.getPlayersWW().size()));
            }

            UUID uuid = (UUID) game.getPlayersWW().keySet().toArray()[i - game.getPlayersWW().size()];
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {

                player.setGameMode(GameMode.ADVENTURE);
                game.clearPlayer(player);
                Inventory inventory = player.getInventory();
                inventory.clear();

                for (int j = 0; j < 40; j++) {
                    inventory.setItem(j, game.getStuffs().getStartLoot().getItem(j));
                }

                player.teleport(game.getPlayersWW().get(uuid).getSpawn());
            }
        } else if (i % 5 == 0 && j == 10) {

            List<PlayerWW> temp = new ArrayList<>(game.getPlayersWW().values());
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
                if(!temp.isEmpty()){
                    createStructure(Material.AIR, temp.get(0).getSpawn());
                    temp.remove(0);
                }
                else {
                    Bukkit.getScheduler().cancelTask(getTaskId());
                }

            }, 1, temp.size());

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (game.getPlayersWW().containsKey(p.getUniqueId())) {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.sendMessage(game.translate("werewolf.announcement.start.message", game.getScore().conversion(game.getConfig().getTimerValues().get("werewolf.menu.timers.invulnerability"))));
                } else {
                    p.teleport(game.getMapManager().getWorld().getSpawnLocation());
                    p.setGameMode(GameMode.SPECTATOR);
                }

                VersionUtils.getVersionUtils().sendTitle(p, game.translate("werewolf.announcement.start.top_title"), game.translate("werewolf.announcement.start.bot_title"), 20, 20, 20);
                Sounds.NOTE_BASS.play(p);
            }

            world.setTime(23000);
            game.getScenarios().updateCompass();
            game.setState(StateLG.START);
            GameTask start = new GameTask(game);
            start.runTaskTimer(main, 0, 5);
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> Bukkit.getPluginManager().callEvent(new DayEvent(1)), 40);
            cancel();
        } else if (i % 5 == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                VersionUtils.getVersionUtils().sendTitle(p, "Start", "§b" + (10 - j), 25, 20, 25);
                Sounds.NOTE_PIANO.play(p);
            }
            j++;
        }
        i++;
    }
}
