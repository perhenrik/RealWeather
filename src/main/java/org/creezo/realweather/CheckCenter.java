package org.creezo.realweather;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author creezo
 */
public class CheckCenter {

    private final RealWeather plugin;

    public CheckCenter(RealWeather plugin) {
        this.plugin = plugin;
    }

    public double getTemperature(Location location, Player player) {
        double temperature = 15;
        try {
            if (location != null) {
            } else {
                location = player.getLocation();
            }
            if (RealWeather.isDebug()) {
                RealWeather.log("Starting temp calculation.");
            }
            World world = ((World) location.getWorld());
            Biome biome = world.getBiome(location.getBlockX(), location.getBlockZ());
            String biomeName = biome.name();
            if (RealWeather.isDebug()) {
                RealWeather.log("Biome: " + biomeName.toUpperCase());
            }
            int startTemp = RealWeather.config.getVariables().getBiomes().getGlobal().getBiomeAverageTemp(biomeName);
            if (RealWeather.isDebug()) {
                RealWeather.log("Biome average temp: " + startTemp);
            }
            double timeMultiplier = Math.sin(Math.toRadians(0.015D * location.getWorld().getTime()));
            if (timeMultiplier > 0) {
                temperature = timeMultiplier * (double) RealWeather.config.getVariables().getBiomes().getGlobal().getBiomeDayNightTempModifier("Day", biomeName);
            } else {
                temperature = Math.abs(timeMultiplier) * (double) RealWeather.config.getVariables().getBiomes().getGlobal().getBiomeDayNightTempModifier("Night", biomeName);
            }
            try {
                if (plugin.isWeatherModuleLoaded()) {
                    if (location.getWorld().hasStorm()) {
                        temperature += RealWeather.config.getVariables().getBiomes().getGlobal().getBiomesWeatherTempModifier(biomeName);
                    }
                    temperature += plugin.getWeather().getWeatherTemp();
                }
            } catch (NullPointerException e) {
                if (RealWeather.isDebug()) {
                    RealWeather.log("Weather module is errorneous. Skipping weather temp.");
                }
            }
            temperature += (location.getY() - RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel()) / (location.getWorld().getMaxHeight() - RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel()) * RealWeather.config.getVariables().getBiomes().getGlobal().getTopTemp();
            temperature += startTemp;
            if (location.getBlock().getLightFromSky() < (byte) 4 && location.getY() < RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel()) {
                double deepModifier;
                if ((double) location.getY() >= (double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.8d) {
                    deepModifier = (((double) location.getY() - ((double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.8d)) / ((double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() - (double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.8d)) + ((((double) location.getY() - (double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.8d) / ((double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() - ((double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.8d)) - 1) * (-0.15d));
                } else if ((double) location.getY() <= (double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.2d) {
                    if (temperature < 0) {
                        temperature = (temperature * -1) / 2;
                    }
                    deepModifier = (((double) location.getY() - ((double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.2d)) / (0 - (double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.2d)) + ((((double) location.getY() - (double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.2d) / (0 - ((double) RealWeather.config.getVariables().getBiomes().getGlobal().getSeaLevel() * 0.2d)) - 1) * (-0.15d));
                } else {
                    deepModifier = 0.15d;
                }
                if (RealWeather.isDebug()) {
                    RealWeather.log("DeepModifier (Number between 1 and 0.15):" + deepModifier);
                }
                temperature = ((temperature - 10) * deepModifier) + 10;
            }
            temperature += checkHeatAround(player, location, RealWeather.config.getVariables().getBiomes().getGlobal().getHeatCheckRadius());
            if (player != null) {
                List<Entity> Entities = player.getNearbyEntities(RealWeather.config.getVariables().getBiomes().getGlobal().getHeatCheckRadius(), RealWeather.config.getVariables().getBiomes().getGlobal().getHeatCheckRadius(), RealWeather.config.getVariables().getBiomes().getGlobal().getHeatCheckRadius());
                for (Entity entity : Entities) {
                    if (entity.getType().isAlive() && temperature <= 25) {
                        temperature += RealWeather.config.getVariables().getBiomes().getGlobal().getPlayerHeat();
                    }
                }
                if(temperature <= (20 - RealWeather.config.getVariables().getBiomes().getGlobal().getBedTemperatureBonus()) & checkPlayerInBed(player)) temperature += RealWeather.config.getVariables().getBiomes().getGlobal().getBedTemperatureBonus();
            }
            if (RealWeather.isDebug()) {
                RealWeather.log("Returning temperature: " + temperature);
            }
        } catch (Exception e) {
            RealWeather.log.log(Level.SEVERE, null, e);
            RealWeather.sendStackReport(e);
        }
        return temperature;
    }

    public static boolean checkRandomGrass(Player player, int range, int tries) {
        Random random = new Random();
        for (int i = 0; i < tries; i++) {
            Block thisBlock = player.getLocation().getBlock().getRelative(random.nextInt((range * 2) + 1) - range, random.nextInt((range * 2) + 1) - range, random.nextInt((range * 2) + 1) - range);
            if (thisBlock.getTypeId() == 31) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkToTop(Block block, int MaxMapHeigh) {
        boolean IsUnderRoof = false;
        int heigh = block.getY();
        while (heigh < MaxMapHeigh) {
            block = block.getRelative(BlockFace.UP);
            if (block.getTypeId() != 0) {
                IsUnderRoof = true;
                break;
            }
            heigh = block.getY();
            IsUnderRoof = false;
        }
        return IsUnderRoof;
    }

    public static boolean checkPlayerInBed(Player player) {
        return player.isSleeping();
    }

    public static boolean checkPlayerInside(Location location, int checkRadius, String recognizer) {
        boolean inside = false;
        boolean checkOnce = true;
        if (recognizer.equals("simple")) {
            if (RealWeather.isDebug()) {
                RealWeather.log("simple selected");
            }
            inside = checkToTop(location.getBlock().getRelative(BlockFace.UP), location.getWorld().getMaxHeight() - 1);
        } else if (recognizer.equals("default")) {
            if (RealWeather.isDebug()) {
                RealWeather.log("default selected");
            }
            int heigh = location.getBlockY();
            int MaxHeigh = location.getWorld().getMaxHeight() - 1;
            if (RealWeather.isDebug()) {
                RealWeather.log("Heigh: " + ConvertIntToString(heigh));
            }
            Block NowCheckingBlock = location.getBlock();
            Block StartBlock = location.getBlock();
            for (int radius = 1; radius <= checkRadius; radius++) {
                if (checkOnce == true) {
                    checkOnce = false;
                    inside = checkToTop(NowCheckingBlock, MaxHeigh);
                    if (inside == false) {
                        break;
                    }
                }

                StartBlock = StartBlock.getRelative(BlockFace.NORTH_WEST);
                NowCheckingBlock = StartBlock;
                int BlockNumInSide = (radius * 2);
                for (int side = 1; side <= 4; side++) {
                    switch (side) {
                        case 1:
                            for (int blocks = 1; blocks <= BlockNumInSide; blocks++) {
                                inside = checkToTop(NowCheckingBlock, MaxHeigh);
                                if (inside == false) {
                                    break;
                                }
                                NowCheckingBlock = NowCheckingBlock.getRelative(BlockFace.EAST);
                            }
                            break;
                        case 2:
                            for (int blocks = 1; blocks <= BlockNumInSide; blocks++) {
                                inside = checkToTop(NowCheckingBlock, MaxHeigh);
                                if (inside == false) {
                                    break;
                                }
                                NowCheckingBlock = NowCheckingBlock.getRelative(BlockFace.SOUTH);
                            }
                            break;
                        case 3:
                            for (int blocks = 1; blocks <= BlockNumInSide; blocks++) {
                                inside = checkToTop(NowCheckingBlock, MaxHeigh);
                                if (inside == false) {
                                    break;
                                }
                                NowCheckingBlock = NowCheckingBlock.getRelative(BlockFace.WEST);
                            }
                            break;
                        case 4:
                            for (int blocks = 1; blocks <= BlockNumInSide; blocks++) {
                                inside = checkToTop(NowCheckingBlock, MaxHeigh);
                                if (inside == false) {
                                    break;
                                }
                                NowCheckingBlock = NowCheckingBlock.getRelative(BlockFace.NORTH);
                            }
                            break;
                    }
                    if (inside == false) {
                        break;
                    }
                }
                if (inside == false) {
                    break;
                }
            }
        } else if (recognizer.equals("cross")) {
            if (RealWeather.isDebug()) {
                RealWeather.log("cross selected");
            }
            Block RangeCheckBlock;
            int heigh = location.getBlockY();
            Block playerPositionBlock = location.getBlock().getRelative(BlockFace.UP);
            int MaxHeigh = location.getWorld().getMaxHeight() - 1;
            if (RealWeather.isDebug()) {
                RealWeather.log("Heigh: " + ConvertIntToString(heigh));
            }
            for (int once = 1; once == 1; once++) {
                inside = checkToTop(playerPositionBlock, MaxHeigh);
                if (inside == false) {
                    break;
                }

                int RangeToNorthSide = 0;
                RangeCheckBlock = playerPositionBlock;
                for (int range = 1; range <= checkRadius; range++) {
                    if (RangeCheckBlock.getRelative(BlockFace.NORTH, range).getTypeId() == 0) {
                        RangeToNorthSide++;
                    } else {
                        break;
                    }
                }

                int RangeToEastSide = 0;
                RangeCheckBlock = playerPositionBlock;
                for (int range = 1; range <= checkRadius; range++) {
                    if (RangeCheckBlock.getRelative(BlockFace.EAST, range).getTypeId() == 0) {
                        RangeToEastSide++;
                    } else {
                        break;
                    }
                }

                int RangeToSouthSide = 0;
                RangeCheckBlock = playerPositionBlock;
                for (int range = 1; range <= checkRadius; range++) {
                    if (RangeCheckBlock.getRelative(BlockFace.SOUTH, range).getTypeId() == 0) {
                        RangeToSouthSide++;
                    } else {
                        break;
                    }
                }

                int RangeToWestSide = 0;
                RangeCheckBlock = playerPositionBlock;
                for (int range = 1; range <= checkRadius; range++) {
                    if (RangeCheckBlock.getRelative(BlockFace.WEST, range).getTypeId() == 0) {
                        RangeToWestSide++;
                    } else {
                        break;
                    }
                }

                Block StartBlock = playerPositionBlock.getRelative(BlockFace.NORTH, RangeToNorthSide);
                StartBlock = StartBlock.getRelative(BlockFace.EAST, RangeToEastSide);
                for (int EastWestSize = 0; EastWestSize <= RangeToWestSide + RangeToEastSide; EastWestSize++) {
                    for (int NorthSouthSize = 0; NorthSouthSize <= RangeToNorthSide + RangeToSouthSide; NorthSouthSize++) {
                        inside = checkToTop(StartBlock.getRelative(NorthSouthSize, 0, EastWestSize), MaxHeigh);
                        if (inside == false) {
                            break;
                        }
                    }
                    if (inside == false) {
                        break;
                    }
                }
            }
        }
        return inside;
    }

    public static Biome checkPlayerBiome(Player player) {
        World world = ((World) player.getLocation().getWorld());
        Biome BiomeType = world.getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
        return BiomeType;
    }

    public static Biome checkPlayerBiome(Location loc) {
        World world = ((World) loc.getWorld());
        Biome BiomeType = world.getBiome(loc.getBlockX(), loc.getBlockZ());
        return BiomeType;
    }

    public static double[] getPlrResist(Player player, String resistType) {
        double[] resist = {1, 0};
        ItemStack WearBoots = player.getInventory().getBoots();
        ItemStack WearChestplate = player.getInventory().getChestplate();
        ItemStack WearHelmet = player.getInventory().getHelmet();
        ItemStack WearLeggings = player.getInventory().getLeggings();
        int BootsID = 0, ChestplateID = 0, HelmetID = 0, LeggingsID = 0;
        try {
            BootsID = WearBoots.getTypeId();
            if (RealWeather.isDebug()) {
                RealWeather.log("BootsID: " + BootsID);
            }
        } catch (Exception ex) {
            if (RealWeather.isDebug()) {
                RealWeather.log("No Boots.");
            }
        }
        try {
            ChestplateID = WearChestplate.getTypeId();
            if (RealWeather.isDebug()) {
                RealWeather.log("ChestplateID: " + ChestplateID);
            }
        } catch (Exception ex) {
            if (RealWeather.isDebug()) {
                RealWeather.log("No Chestplate.");
            }
        }
        try {
            HelmetID = WearHelmet.getTypeId();
            if (RealWeather.isDebug()) {
                RealWeather.log("HelmetID: " + HelmetID);
            }
        } catch (Exception ex) {
            if (RealWeather.isDebug()) {
                RealWeather.log("No Helmet.");
            }
        }
        try {
            LeggingsID = WearLeggings.getTypeId();
            if (RealWeather.isDebug()) {
                RealWeather.log("LeggingsID: " + LeggingsID);
            }
        } catch (Exception ex) {
            if (RealWeather.isDebug()) {
                RealWeather.log("No Leggings.");
            }
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("BootsID: " + BootsID);
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("ChestplateID: " + ChestplateID);
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("HelmetID: " + HelmetID);
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("LeggingsID: " + LeggingsID);
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("Resist2(0): " + resist[0]);
        }
        if (BootsID != 0) {
            double[] vars = RealWeather.getArmors().getResistance(BootsID, resistType);
            resist[0] *= vars[0];
            resist[1] += vars[1];
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("Resist2(1): " + resist[0]);
        }
        if (ChestplateID != 0) {
            double[] vars = RealWeather.getArmors().getResistance(ChestplateID, resistType);
            resist[0] *= vars[0];
            resist[1] += vars[1];
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("Resist2(2): " + resist[0]);
        }
        if (HelmetID != 0) {
            double[] vars = RealWeather.getArmors().getResistance(HelmetID, resistType);
            resist[0] *= vars[0];
            resist[1] += vars[1];
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("Resist2(3): " + resist[0]);
        }
        if (LeggingsID != 0) {
            double[] vars = RealWeather.getArmors().getResistance(LeggingsID, resistType);
            resist[0] *= vars[0];
            resist[1] += vars[1];
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("Resist2(4): " + resist[0]);
        }
        return resist;
    }

    public double checkHeatAround(Player player, Location location, int HeatCheckRadius) {
        if (RealWeather.isDebug()) {
            RealWeather.log("Checking heat...");
        }
        double NumOfTorches = 1;
        double Temperature = 0;
        double BlockPower;
        double rangeDouble;
        double varOne;
        boolean cooler;
        Block playerBlock = location.getBlock();
        Block startBlock = playerBlock.getRelative(HeatCheckRadius * (-1) - 1, (HeatCheckRadius * (-1)), HeatCheckRadius * (-1) - 1);
        for (int x = 1; x <= (HeatCheckRadius * 2) + 1; x++) {
            for (int z = 1; z <= (HeatCheckRadius * 2) + 1; z++) {
                for (int y = 1; y <= (HeatCheckRadius * 2); y++) {
                    if (plugin.heatSources.containsKey(startBlock.getRelative(x, y, z).getType())) {
                        BlockPower = plugin.heatSources.get(startBlock.getRelative(x, y, z).getType());
                        if (RealWeather.config.getVariables().getBiomes().getGlobal().isTorchesFading() && startBlock.getRelative(x, y, z).getType().equals(Material.TORCH)) {
                            BlockPower /= NumOfTorches;
                            NumOfTorches++;
                        }
                        cooler = BlockPower < 0;
                    } else {
                        BlockPower = 0;
                        cooler = false;
                    }
                    if (BlockPower != 0) {
                        rangeDouble = startBlock.getRelative(x, y, z).getLocation().distance(playerBlock.getLocation());
                        varOne = BlockPower * (1 - (rangeDouble / (HeatCheckRadius * 2)));
                        if (varOne >= 0.0d && cooler == false) {
                            Temperature += varOne;
                        } else if (varOne <= 0.0d && cooler == true) {
                            Temperature += varOne;
                        }
                    }
                }
            }
        }
        if (player != null) {
            if (plugin.heatInHand.containsKey(player.getItemInHand().getType())) {
                BlockPower = plugin.heatInHand.get(player.getItemInHand().getType());
            } else {
                BlockPower = 0;
            }
        } else {
            BlockPower = 0;
        }
        if (RealWeather.isDebug()) {
            RealWeather.log("From item in hand: " + ConvertIntToString((int) BlockPower));
        }
        Temperature += BlockPower;
        if (RealWeather.isDebug()) {
            RealWeather.log("Total heat from blocks and items: " + Temperature);
        }
        return Temperature;
    }

    private static String ConvertIntToString(int number) {
        return "" + number;
    }
}