/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = Info.java >> Generated by: Citymonstret at 2014-08-09 01:41
 */

package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.database.DBFunc;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Citymonstret
 */
public class Info extends SubCommand {

	public Info() {
		super(Command.INFO, "Display plot info", "info", CommandCategory.INFO, false);
	}

	@Override
	public boolean execute(Player player, String... args) {
		
		Plot plot;
		if (player!=null) {
			if (!PlayerFunctions.isInPlot(player)) {
				PlayerFunctions.sendMessage(player, C.NOT_IN_PLOT);
				return false;
			}
			plot = PlayerFunctions.getCurrentPlot(player);
		}
		else {
			if (args.length!=2) {
				PlayerFunctions.sendMessage(player, C.INFO_SYNTAX_CONSOLE);
				return false;
			}
			PlotWorld plotworld = PlotMain.getWorldSettings(args[0]);
			if (plotworld==null) {
				PlayerFunctions.sendMessage(player, C.NOT_VALID_WORLD);
				return false;
			}
			try {
				String[] split = args[1].split(";");
				PlotId id = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
				plot = PlotHelper.getPlot(Bukkit.getWorld(plotworld.worldname), id);
				if (plot==null) {
					PlayerFunctions.sendMessage(player, C.NOT_VALID_PLOT_ID);
				}
			}
			catch (Exception e) {
				PlayerFunctions.sendMessage(player, C.INFO_SYNTAX_CONSOLE);
				return false;
			}
		}

		boolean hasOwner = plot.hasOwner();
		boolean containsEveryone;
		boolean trustedEveryone;

		// Wildcard player {added}
		{
			if (plot.helpers == null) {
				containsEveryone = false;
			}
			else {
				containsEveryone = plot.helpers.contains(DBFunc.everyone);
			}
			if (plot.trusted == null) {
				trustedEveryone = false;
			}
			else {
				trustedEveryone = plot.trusted.contains(DBFunc.everyone);
			}
		}

		// Unclaimed?
		if (!hasOwner && !containsEveryone && !trustedEveryone) {
			PlayerFunctions.sendMessage(player, C.PLOT_INFO_UNCLAIMED, plot.id.x + ";" + plot.id.y);
			return true;
		}

		new StringBuilder();

		String owner = "none";
		if (plot.owner != null) {
			owner = Bukkit.getOfflinePlayer(plot.owner).getName();
		}
		if (owner == null) {
			owner = plot.owner.toString();
		}

		String info = C.PLOT_INFO.s();
		info = info.replaceAll("%alias%", plot.settings.getAlias().length() > 0 ? plot.settings.getAlias() : "none");
		info = info.replaceAll("%id%", plot.id.toString());
		info = info.replaceAll("%biome%", getBiomeAt(plot).toString());
		info = info.replaceAll("%owner%", owner);
		info = info.replaceAll("%helpers%", getPlayerList(plot.helpers));
		info = info.replaceAll("%trusted%", getPlayerList(plot.trusted));
		info = info.replaceAll("%denied%", getPlayerList(plot.denied));
		info = info.replaceAll("%rating%", "" + DBFunc.getRatings(plot));
		info =
				info.replaceAll("%flags%", StringUtils.join(plot.settings.getFlags(), "").length() > 0
						? StringUtils.join(plot.settings.getFlags(), ",") : "none");
		// PlayerFunctions.sendMessage(player,
		// PlayerFunctions.getTopPlot(player.getWorld(), plot).id.toString());
		// PlayerFunctions.sendMessage(player,
		// PlayerFunctions.getBottomPlot(player.getWorld(),
		// plot).id.toString());
		PlayerFunctions.sendMessage(player, info);
		return true;
	}

	private String getPlayerList(ArrayList<UUID> l) {
		if ((l == null) || (l.size() < 1)) {
			return " none";
		}
		String c = C.PLOT_USER_LIST.s();
		StringBuilder list = new StringBuilder();
		for (int x = 0; x < l.size(); x++) {
			if ((x + 1) == l.size()) {
				list.append(c.replace("%user%", getPlayerName(l.get(x))).replace(",", ""));
			}
			else {
				list.append(c.replace("%user%", getPlayerName(l.get(x))));
			}
		}
		return list.toString();
	}

	private String getPlayerName(UUID uuid) {
		if (uuid == null) {
			return "unknown";
		}
		if (uuid.equals(DBFunc.everyone) || uuid.toString().equalsIgnoreCase(DBFunc.everyone.toString())) {
			return "everyone";
		}
		/*
		 * OfflinePlayer plr = Bukkit.getOfflinePlayer(uuid); if (plr.getName()
		 * == null) { return "unknown"; } return plr.getName();
		 */
		return UUIDHandler.getName(uuid);
	}

	private Biome getBiomeAt(Plot plot) {
		World w = Bukkit.getWorld(plot.world);
		Location bl = PlotHelper.getPlotTopLoc(w, plot.id);
		return bl.getBlock().getBiome();
	}
}
