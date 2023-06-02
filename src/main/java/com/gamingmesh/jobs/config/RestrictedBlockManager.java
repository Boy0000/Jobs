package com.gamingmesh.jobs.config;

import java.util.HashMap;
import java.util.Map;

import com.gamingmesh.jobs.Jobs;

import com.gamingmesh.jobs.hooks.HookManager;
import com.gamingmesh.jobs.hooks.JobsHook;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Messages.CMIMessages;
import uk.antiperson.stackmob.hook.Hook;

public class RestrictedBlockManager {

    public final Map<CMIMaterial, Integer> restrictedBlocksTimer = new HashMap<>();
    public final Map<String, Integer> restrictedOraxenBlocksTimer = new HashMap<>();

    /**
     * Method to load the restricted blocks configuration
     * loads from Jobs/restrictedBlocks.yml
     */
    public void load() {
        if (!Jobs.getGCManager().useBlockProtection)
            return;

        ConfigReader cfg = null;
        try {
            cfg = new ConfigReader(Jobs.getInstance(), "restrictedBlocks.yml");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cfg == null)
            return;

        cfg.addComment("blocksTimer", "Block protected by timer in sec",
            "Category name can be any you like to be easily recognized",
            "id can be actual block id (use /jobs blockinfo to get correct id) or use block name",
            "By setting time to -1 will keep block protected until global cleanup, mainly used for structure blocks like diamond",
            "Set to 0 if you want to disable protection on specific blocks",
            "If you want to have default value for all blocks, enable GlobalBlockTimer in generalConfig file");

        org.bukkit.configuration.ConfigurationSection section = cfg.getC().getConfigurationSection("blocksTimer");
        if (section != null) {
            for (String one : section.getKeys(false)) {
                if ((section.isString(one + ".id") || section.isInt(one + ".id")) && section.isInt(one + ".cd")) {
                    int cd = section.getInt(one + ".cd");
                    String id = section.getString(one + ".id");
                    CMIItemStack cm = CMIMaterial.get(id).newCMIItemStack();
                    CMIMaterial mat = cm == null ? null : cm.getCMIType();

                    if (JobsHook.Oraxen.isEnabled() && HookManager.getOraxenManager().isOraxenBlockFurniture(id)) {
                        restrictedOraxenBlocksTimer.put(id, cd);
                        cfg.set("blocksTimer." + id, cd);
                        continue;
                    } else {
                        if (mat == null || !mat.isBlock()) {
                            CMIMessages.consoleMessage("&eYour defined (&6" + one + "&e) protected block id/name is not correct!");
                            continue;
                        }

                        restrictedBlocksTimer.put(mat, cd);
                        cfg.set("blocksTimer." + mat.name(), cd);
                    }

                } else {
                    CMIMaterial mat = CMIMaterial.get(one);
                    String oraxenId = JobsHook.Oraxen.isEnabled() && HookManager.getOraxenManager().isOraxenBlockFurniture(one) ? one : null;
                    if (mat == CMIMaterial.NONE && oraxenId == null) continue;

                    int timer = cfg.get("blocksTimer." + one, -99);
                    if (timer == -99) {
                        cfg.set("blocksTimer." + one, null);
                        continue;
                    }

                    cfg.set("blocksTimer." + one, null);
                    cfg.get("blocksTimer." + mat.name(), timer);

                    if (oraxenId != null) {
                        restrictedOraxenBlocksTimer.put(oraxenId, timer);
                    } else {
                        if (!mat.isBlock()) {
                            CMIMessages.consoleMessage("&e[Jobs] Your defined (" + one + ") protected block id/name is not correct!");
                            continue;
                        }

                        restrictedBlocksTimer.put(mat, timer);
                    }
                }
            }
        }

        int size = restrictedBlocksTimer.size();
        if (size > 0)
            CMIMessages.consoleMessage("&eLoaded &6" + size + " &eprotected blocks timers");

        int oraxenSize = restrictedOraxenBlocksTimer.size();
        if (oraxenSize > 0)
            CMIMessages.consoleMessage("&eLoaded &6" + oraxenSize + " &eprotected oraxen blocks timers");

        cfg.save();
    }
}
