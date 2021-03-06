package io.github.ph1lou.werewolfplugin.guis;


import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import io.github.ph1lou.werewolfapi.ConfigRegister;
import io.github.ph1lou.werewolfapi.ConfigWereWolfAPI;
import io.github.ph1lou.werewolfapi.enumlg.UniversalMaterial;
import io.github.ph1lou.werewolfapi.utils.ItemBuilder;
import io.github.ph1lou.werewolfplugin.Main;
import io.github.ph1lou.werewolfplugin.game.GameManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class GlobalConfigs implements InventoryProvider {


    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("global")
            .manager(JavaPlugin.getPlugin(Main.class).getInvManager())
            .provider(new GlobalConfigs())
            .size(Math.min(54, (JavaPlugin.getPlugin(Main.class).getRegisterConfigs().size() / 9 + 2) * 9) / 9, 9)
            .title(JavaPlugin.getPlugin(Main.class).getCurrentGame().translate("werewolf.menu.global.name"))
            .closeable(true)
            .build();


    @Override
    public void init(Player player, InventoryContents contents) {
        Main main = JavaPlugin.getPlugin(Main.class);
        GameManager game = main.getCurrentGame();

        contents.set(0, 0, ClickableItem.of((new ItemBuilder(UniversalMaterial.COMPASS.getType()).setDisplayName(game.translate("werewolf.menu.return")).build()), e -> Config.INVENTORY.open(player)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

        Main main = JavaPlugin.getPlugin(Main.class);
        GameManager game = main.getCurrentGame();
        ConfigWereWolfAPI config = game.getConfig();
        Pagination pagination = contents.pagination();
        List<ClickableItem> items = new ArrayList<>();

        for (ConfigRegister ConfigRegister : main.getRegisterConfigs()) {
            String key = ConfigRegister.getKey();
            List<String> lore = new ArrayList<>(ConfigRegister.getLore());
            ItemStack itemStack;

            if (game.getConfig().getConfigValues().get(ConfigRegister.getKey())) {
                lore.add(0, game.translate("werewolf.utils.enable", ""));
                itemStack = UniversalMaterial.GREEN_TERRACOTTA.getStack();
            } else {
                lore.add(0, game.translate("werewolf.utils.disable", ""));
                itemStack = UniversalMaterial.RED_TERRACOTTA.getStack();
            }
            items.add(ClickableItem.of((new ItemBuilder(itemStack).setDisplayName(game.translate(ConfigRegister.getKey())).setLore(lore).build()), e -> {
                config.getConfigValues().put(key, !config.getConfigValues().get(key));
                if (key.equals("werewolf.menu.global.compass_middle")) {
                    game.getScenarios().updateCompass();
                }
            }));
        }

        if (items.size() > 45) {
            pagination.setItems(items.toArray(new ClickableItem[0]));
            pagination.setItemsPerPage(36);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));
            int page = pagination.getPage() + 1;
            contents.set(5, 0, null);
            contents.set(5, 1, null);
            contents.set(5, 3, null);
            contents.set(5, 5, null);
            contents.set(5, 7, null);
            contents.set(5, 8, null);
            contents.set(5, 2, ClickableItem.of(new ItemBuilder(Material.ARROW).setDisplayName(game.translate("werewolf.menu.roles.previous", page, pagination.isFirst() ? page : page - 1)).build(),
                    e -> INVENTORY.open(player, pagination.previous().getPage())));
            contents.set(5, 6, ClickableItem.of(new ItemBuilder(Material.ARROW).setDisplayName(game.translate("werewolf.menu.roles.next", page, pagination.isLast() ? page : page + 1)).build(),
                    e -> INVENTORY.open(player, pagination.next().getPage())));
            contents.set(5, 4, ClickableItem.empty(new ItemBuilder(UniversalMaterial.SIGN.getType()).setDisplayName(game.translate("werewolf.menu.roles.current", page, items.size() / 36 + 1)).build()));
        } else {
            int i = 0;
            for (ClickableItem clickableItem : items) {
                contents.set(i / 9 + 1, i % 9, clickableItem);
                i++;
            }
            for (int k = i; k < (i / 9 + 1) * 9; k++) {
                contents.set(k / 9 + 1, k % 9, null);
            }
        }


    }
}

