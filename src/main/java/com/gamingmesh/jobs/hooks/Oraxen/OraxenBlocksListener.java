package com.gamingmesh.jobs.hooks.Oraxen;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.OraxenBlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.FastPayment;
import com.gamingmesh.jobs.listeners.JobsPaymentListener;
import io.th0rgal.oraxen.api.events.*;
import io.th0rgal.oraxen.mechanics.Mechanic;
import net.Zrips.CMILib.Items.CMIItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OraxenBlocksListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceJob(OraxenNoteBlockPlaceEvent event) {
        addJobAction(ActionType.ORAXEN_PLACE, event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceJob(OraxenStringBlockPlaceEvent event) {
        addJobAction(ActionType.ORAXEN_PLACE, event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceJob(OraxenFurniturePlaceEvent event) {
        addJobAction(ActionType.ORAXEN_PLACE, event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreakJob(OraxenNoteBlockBreakEvent event) {
        addJobAction(ActionType.ORAXEN_BREAK, event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreakJob(OraxenStringBlockBreakEvent event) {
        addJobAction(ActionType.ORAXEN_BREAK, event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreakJob(OraxenFurnitureBreakEvent event) {
        addJobAction(ActionType.ORAXEN_BREAK, event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    private void addJobAction(ActionType type, Player player, Mechanic mechanic, Block block) {
        if (Jobs.getGCManager().disablePaymentIfRiding && player.isInsideVehicle()) return;

        if (!JobsPaymentListener.payIfCreative(player)) return;

        if (!Jobs.getPermissionHandler().hasWorldPermission(player, player.getWorld().getName())) return;

        OraxenBlockActionInfo bInfo = new OraxenBlockActionInfo(mechanic.getItemID(), type);

        FastPayment fp = Jobs.FASTPAYMENT.get(player.getUniqueId());
        if (fp != null) {
            if (fp.getTime() > System.currentTimeMillis() && (fp.getInfo().getName().equals(bInfo.getName()) ||
                    fp.getInfo().getNameWithSub().equals(bInfo.getNameWithSub()))
            ) {
                Jobs.perform(fp.getPlayer(), fp.getInfo(), fp.getPayment(), fp.getJob(), block, null, null);
                return;
            }
            Jobs.FASTPAYMENT.remove(player.getUniqueId());
        }
        if (!JobsPaymentListener.payForItemDurabilityLoss(player)) return;

        // Protection for block break with silktouch

        // Protection for block break with silktouch
        if (Jobs.getGCManager().useSilkTouchProtection) {
            ItemStack item = CMIItemStack.getItemInMainHand(player);
            if (item.getType() != Material.AIR && Jobs.getBpManager().isInBp(block)) {
                if (item.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    return;
                }
            }
        }

        Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), bInfo, block);
    }
}
