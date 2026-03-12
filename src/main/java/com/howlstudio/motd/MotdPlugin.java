package com.howlstudio.motd;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
/**
 * MotdManager — Fully customizable MOTD shown on join.
 * Multi-line, color codes, placeholders: {player}, {online}, {date}
 * /motd set <text> — set MOTD (admin)
 * /motd — view MOTD
 */
public final class MotdPlugin extends JavaPlugin {
    private String motd = "§6Welcome to the server, §e{player}§6!\n§7Players online: §f{online}\n§aHave fun!";
    private Path dataDir;
    public MotdPlugin(JavaPluginInit init) { super(init); }
    @Override protected void setup() {
        System.out.println("[MOTD] Loading...");
        dataDir = getDataDirectory();
        try { Files.createDirectories(dataDir); } catch (Exception e) {}
        load();
        HytaleServer.get().getEventBus().registerGlobal(PlayerReadyEvent.class, e -> {
            Player p = e.getPlayer(); if (p == null) return;
            PlayerRef ref = p.getPlayerRef(); if (ref == null) return;
            showMotd(ref);
        });
        CommandManager.get().register(new AbstractPlayerCommand("motd", "Show MOTD. /motd set <text> to update (admin).") {
            @Override protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef pl, World world) {
                String input = ctx.getInputString().trim();
                if (input.toLowerCase().startsWith("set ")) {
                    motd = input.substring(4); save();
                    pl.sendMessage(Message.raw("[MOTD] Updated!"));
                } else { showMotd(pl); }
            }
        });
        System.out.println("[MOTD] Ready.");
    }
    private void showMotd(PlayerRef ref) {
        int online = 0; try { online = Universe.get().getPlayers().size(); } catch (Exception e) {}
        String date = java.time.LocalDate.now().toString();
        String text = motd.replace("{player}", ref.getUsername()).replace("{online}", String.valueOf(online)).replace("{date}", date);
        for (String line : text.split("\\\\n|\n")) ref.sendMessage(Message.raw(line));
    }
    private void save() { try { Files.writeString(dataDir.resolve("motd.txt"), motd); } catch (Exception e) {} }
    private void load() { try { Path f = dataDir.resolve("motd.txt"); if (Files.exists(f)) motd = Files.readString(f); } catch (Exception e) {} }
    @Override protected void shutdown() { save(); System.out.println("[MOTD] Stopped."); }
}
