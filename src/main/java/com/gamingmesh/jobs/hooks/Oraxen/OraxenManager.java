package com.gamingmesh.jobs.hooks.Oraxen;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.hooks.MythicMobs.MythicMobs5Listener;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.Zrips.CMILib.Messages.CMIMessages;
import org.bukkit.plugin.Plugin;

public class OraxenManager {

    private Jobs plugin;

    public OraxenManager(Jobs plugin) {
        this.plugin = plugin;
    }

    public boolean check() {
        Plugin oraxen = plugin.getServer().getPluginManager().getPlugin("Oraxen");
        if (oraxen == null) return false;

        CMIMessages.consoleMessage("&e[Jobs] &3Oraxen was found - Enabling capabilities.");
        return true;
    }

    public void registerListener() {
        plugin.getServer().getPluginManager().registerEvents(new OraxenBlocksListener(), plugin);
    }
}
