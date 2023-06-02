package com.gamingmesh.jobs.hooks.Oraxen;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.actions.OraxenBlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.FastPayment;
import com.gamingmesh.jobs.listeners.JobsPaymentListener;
import io.th0rgal.oraxen.api.events.*;
import io.th0rgal.oraxen.mechanics.Mechanic;
import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static com.gamingmesh.jobs.listeners.JobsPaymentListener.payIfCreative;

public class OraxenBlocksListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceJob(OraxenNoteBlockPlaceEvent event) {
        addJobPlaceAction(event.getPlayer(), event.getItemInHand(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceJob(OraxenStringBlockPlaceEvent event) {
        addJobPlaceAction(event.getPlayer(), event.getItemInHand(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceJob(OraxenFurniturePlaceEvent event) {
        addJobPlaceAction(event.getPlayer(), event.getItemInHand(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreakJob(OraxenNoteBlockBreakEvent event) {
        addJobBreakAction(event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreakJob(OraxenStringBlockBreakEvent event) {
        addJobBreakAction(event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreakJob(OraxenFurnitureBreakEvent event) {
        addJobBreakAction(event.getPlayer(), event.getMechanic(), event.getBlock());
    }

    private void addJobPlaceAction(Player player, ItemStack itemInHand, Mechanic mechanic, Block block) {
        // A tool should not trigger a BlockPlaceEvent (fixes stripping logs bug #940)
        // Allow this to trigger with a hoe so players can get paid for farmland.
        if (CMIMaterial.get(itemInHand.getType()).isTool() && !itemInHand.getType().toString().endsWith("_HOE"))
            return;

        if (!Jobs.getGCManager().canPerformActionInWorld(block.getWorld()))
            return;

        if (Version.isCurrentEqualOrLower(Version.v1_12_R1)
                && CMILib.getInstance().getItemManager().getItem(itemInHand).isSimilar(CMIMaterial.BONE_MEAL.newCMIItemStack()))
            return;

        // check if player is riding
        if (Jobs.getGCManager().disablePaymentIfRiding && player.isInsideVehicle())
            return;

        // check if in creative
        if (!payIfCreative(player))
            return;

        if (!Jobs.getPermissionHandler().hasWorldPermission(player, player.getLocation().getWorld().getName()))
            return;

        OraxenBlockActionInfo bInfo = new OraxenBlockActionInfo(mechanic.getItemID(), ActionType.ORAXEN_PLACE);
        Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), bInfo, block);
        Bukkit.broadcast(Component.text("placed oraxen-block"));
    }

    private void addJobBreakAction(Player player, Mechanic mechanic, Block block) {
        if (Jobs.getGCManager().disablePaymentIfRiding && player.isInsideVehicle()) return;

        if (!payIfCreative(player)) return;

        if (!Jobs.getPermissionHandler().hasWorldPermission(player, player.getWorld().getName())) return;

        OraxenBlockActionInfo bInfo = new OraxenBlockActionInfo(mechanic.getItemID(), ActionType.ORAXEN_BREAK);

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
        Bukkit.broadcast(Component.text("broke oraxen-block"));
    }
}
