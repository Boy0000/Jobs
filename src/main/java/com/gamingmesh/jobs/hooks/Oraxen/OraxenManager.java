package com.gamingmesh.jobs.hooks.Oraxen;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.hooks.JobsHook;
import com.gamingmesh.jobs.hooks.MythicMobs.MythicMobs5Listener;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import net.Zrips.CMILib.Messages.CMIMessages;
import org.bukkit.plugin.Plugin;
import org.bukkit.block.Block;

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

    public boolean isOraxenBlockFurniture(String id) {
        if (JobsHook.Oraxen.isEnabled()) return OraxenBlocks.isOraxenBlock(id) || OraxenFurniture.isFurniture(id);
        else return false;
    }

    public String getOraxenBlockFurnitureId(Block block) {
        if (JobsHook.Oraxen.isEnabled()) {
            if (OraxenBlocks.isOraxenBlock(block)) return OraxenBlocks.getOraxenBlock(block.getBlockData()).getItemID();
            else if (OraxenFurniture.isFurniture(block)) return OraxenFurniture.getFurnitureMechanic(block).getItemID();
            else return null;
        }
        else return null;
    }

    public void registerListener() {
        plugin.getServer().getPluginManager().registerEvents(new OraxenBlocksListener(), plugin);
    }
}
