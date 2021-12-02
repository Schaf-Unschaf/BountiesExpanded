package de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.scripts.combat.effects.StatApplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.scripts.combat.effects.esu.SystemUpgrades.UpgradeTypes.*;

@Log4j
public class SystemUpgrades {
    @Getter
    @AllArgsConstructor
    public enum UpgradeCategories {
        ENGINE("Tuned Engines", "", 3,
                Arrays.asList(MAX_SPEED, ACCELERATION, TURN_RATE, ZERO_FLUX_SPEED, BURN_LEVEL, FUEL_USE)),
        FLUX("Improved Flux Grid", "", 3,
                Arrays.asList(FLUX_CAPACITY, FLUX_DISSIPATION, HARD_FLUX_DISSIPATION, VENT_RATE, ZERO_FLUX_LIMIT, OVERLOAD_DURATION)),
        SENSOR("Additional Sensors", "", 3,
                Arrays.asList(SENSOR_STRENGTH, SENSOR_PROFILE, SIGHT_RADIUS, AUTOFIRE_ACCURACY)),
        SHIELD("Calibrated Shields", "", 3,
                Arrays.asList(SHIELD_UPKEEP, SHIELD_ABSORPTION, SHIELD_TURN_RATE, SHIELD_UNFOLD, SHIELD_ARC)),
        DURABILITY("Reinforced Structure", "", 3,
                Arrays.asList(ARMOR_BONUS, HULL_BONUS, ENGINE_HEALTH, WEAPON_HEALTH, ENGINE_REPAIR, WEAPON_REPAIR)),
        RESISTANCE("Forged Plating", "", 3,
                Arrays.asList(ENERGY_ARMOR_DAMAGE_TAKEN, KINETIC_ARMOR_DAMAGE_TAKEN, EXPLOSIVE_ARMOR_DAMAGE_TAKEN, FRAGMENTATION_ARMOR_DAMAGE_TAKEN,
                        ENERGY_SHIELD_DAMAGE_TAKEN, KINETIC_SHIELD_DAMAGE_TAKEN, EXPLOSIVE_SHIELD_DAMAGE_TAKEN, FRAGMENTATION_SHIELD_DAMAGE_TAKEN, EMP_DAMAGE_TAKEN)),
        WEAPON("Upgraded Weapons", "", 3,
                Arrays.asList(WEAPON_TURN_RATE, WEAPON_RECOIL, BALLISTIC_WEAPON_RANGE, ENERGY_WEAPON_RANGE, BALLISTIC_ROF, ENERGY_ROF,
                        MISSILE_RANGE, MISSILE_HEALTH, MISSILE_GUIDANCE, MISSILE_RELOAD, ECCM_CHANCE)),
        PROJECTILE("Specialised Ammunition", "", 3,
                Arrays.asList(BALLISTIC_DAMAGE, ENERGY_DAMAGE, BEAM_DAMAGE, MISSILE_DAMAGE, HIT_STRENGTH, SHIELD_DAMAGE, ENERGY_FLUX_COST, BALLISTIC_FLUX_COST, PROJECTILE_SPEED, MISSILE_SPEED)),
        LOGISTIC("Planned Logistics", "", 3,
                Arrays.asList(CR_PEAK, CR_MAX, CR_LOSS, CR_DEPLOYMENT, WEAPON_MALFUNCTION, ENGINE_MALFUNCTION, SHIELD_MALFUNCTION, CRITICAL_MALFUNCTION));

        String title;
        String image;
        int maxNumUpgrades;
        List<UpgradeTypes> possibleUpgrades;

        public static List<UpgradeTypes> getRandomUpgrades(UpgradeCategories category, Random random) {
            WeightedRandomPicker<UpgradeTypes> picker = new WeightedRandomPicker<>(random);
            List<UpgradeTypes> pickedUpgrades = new ArrayList<>();
            int numUpgradesToPick = random.nextInt(category.maxNumUpgrades) + 1;

            picker.addAll(category.possibleUpgrades);
            for (int i = 0; i < numUpgradesToPick; i++)
                pickedUpgrades.add(picker.pickAndRemove());

            return pickedUpgrades;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum UpgradeTypes {
        MAX_SPEED(.20f), ACCELERATION(.50f), TURN_RATE(.50f), ZERO_FLUX_SPEED(.25f), BURN_LEVEL(1.5f), FUEL_USE(.30f),

        FLUX_CAPACITY(.30f), FLUX_DISSIPATION(.30f), HARD_FLUX_DISSIPATION(.5f), VENT_RATE(.30f), ZERO_FLUX_LIMIT(.2f), OVERLOAD_DURATION(.25f),

        SENSOR_STRENGTH(.50f), SENSOR_PROFILE(.35f), SIGHT_RADIUS(.40f), AUTOFIRE_ACCURACY(.40f),

        SHIELD_UPKEEP(.25f), SHIELD_ABSORPTION(.15f), SHIELD_TURN_RATE(.50f), SHIELD_UNFOLD(.50f), SHIELD_ARC(.35f),

        ARMOR_BONUS(.25f), HULL_BONUS(.25f), ENGINE_HEALTH(.50f), WEAPON_HEALTH(.50f), ENGINE_REPAIR(.50f), WEAPON_REPAIR(.50f),

        ENERGY_ARMOR_DAMAGE_TAKEN(.20f), KINETIC_ARMOR_DAMAGE_TAKEN(.20f), EXPLOSIVE_ARMOR_DAMAGE_TAKEN(.20f), FRAGMENTATION_ARMOR_DAMAGE_TAKEN(.20f),
        ENERGY_SHIELD_DAMAGE_TAKEN(.20f), KINETIC_SHIELD_DAMAGE_TAKEN(.20f), EXPLOSIVE_SHIELD_DAMAGE_TAKEN(.20f), FRAGMENTATION_SHIELD_DAMAGE_TAKEN(.20f), EMP_DAMAGE_TAKEN(.30f),

        WEAPON_TURN_RATE(.50f), WEAPON_RECOIL(.40f), BALLISTIC_WEAPON_RANGE(.25f), ENERGY_WEAPON_RANGE(.25f), BALLISTIC_ROF(.20f), ENERGY_ROF(.20f),
        MISSILE_RANGE(.35f), MISSILE_HEALTH(.50f), MISSILE_GUIDANCE(.35f), MISSILE_RELOAD(.40f), ECCM_CHANCE(.20f),

        BALLISTIC_DAMAGE(.15f), ENERGY_DAMAGE(.15f), BEAM_DAMAGE(.15f),
        MISSILE_DAMAGE(.15f), HIT_STRENGTH(.25f), SHIELD_DAMAGE(.15f),
        ENERGY_FLUX_COST(.15f), BALLISTIC_FLUX_COST(.15f), PROJECTILE_SPEED(.30f), MISSILE_SPEED(.30f),

        CR_PEAK(.30f), CR_MAX(.10f), CR_LOSS(.25f), CR_DEPLOYMENT(.25f), WEAPON_MALFUNCTION(.40f), ENGINE_MALFUNCTION(.40f), SHIELD_MALFUNCTION(.40f), CRITICAL_MALFUNCTION(.40f);

        float maxValue;
    }

    @Getter
    @AllArgsConstructor
    public enum UpgradeQuality {
        SALVAGED("Salvaged", 30, .5f, .75f, new Color(150, 40, 40)),
        CIVILIAN("Civilian", 50, .8f, .50f, new Color(200, 200, 200)),
        MILITARY("Military", 30, 1f, .40f, new Color(0, 180, 50)),
        EXPERIMENTAL("Experimental", 12, 1.3f, .30f, new Color(0, 150, 255)),
        REMNANT("Remnant", 5, 1.7f, .20f, new Color(70, 255, 235)),
        DOMAIN("Domain", 1, 2f, .10f, new Color(255, 120, 0));

        String name;
        int weightingModifier;
        float qualityModifier;
        float chanceForNegativeStats;
        Color color;

        public static UpgradeQuality getRandomQuality(Random random) {
            WeightedRandomPicker<UpgradeQuality> picker = new WeightedRandomPicker<>(random);
            UpgradeQuality[] values = values();
            for (int i = 0; i < values.length; i++)
                picker.add(values[i], values[i].weightingModifier);

            return picker.pick();
        }
    }

    protected static UpgradeCategories getRandomUpgrade(Random random) {
        SystemUpgrades.UpgradeCategories[] upgradeTypes = SystemUpgrades.UpgradeCategories.values();
        return upgradeTypes[random.nextInt(upgradeTypes.length)];
    }

    public static List<StatApplier> getAllCategories(@NotNull Random random) {
        List<StatApplier> allCategories = new ArrayList<>();
        allCategories.add(new SystemUpgradeEngines(random));
        allCategories.add(new SystemUpgradeFlux(random));
        allCategories.add(new SystemUpgradeSensor(random));
        allCategories.add(new SystemUpgradeDurability(random));
        allCategories.add(new SystemUpgradeResistance(random));
        allCategories.add(new SystemUpgradeShield(random));
        allCategories.add(new SystemUpgradeWeapon(random));
        allCategories.add(new SystemUpgradeProjectile(random));
        allCategories.add(new SystemUpgradeLogistic(random));

        return allCategories;
    }
}
