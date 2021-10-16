package de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.highvaluebounty;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import de.schafunschaf.bountiesexpanded.helper.ui.DescriptionUtils;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.BaseBountyIntel;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResult;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.bounties.BountyResultType;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.difficulty.Difficulty;
import de.schafunschaf.bountiesexpanded.scripts.campaign.intel.entity.BountyEntity;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNotNull;
import static de.schafunschaf.bountiesexpanded.util.ComparisonTools.isNull;

public class HighValueBountyEntity implements BountyEntity {
    private final String bountyId;
    private final int baseReward;
    private final String intelText;
    private final FactionAPI offeringFaction;
    private final FactionAPI targetedFaction;
    private final CampaignFleetAPI fleet;
    private final PersonAPI person;
    private final SectorEntityToken hideout;
    private final float repReward;
    String levelDesc;
    String skillDesc;
    String fleetDesc;
    final String shipType;
    final FleetMemberAPI flagship;
    public static final Logger log = Global.getLogger(HighValueBountyEntity.class);

    public HighValueBountyEntity(int baseReward, float repReward, FactionAPI offeringFaction, FactionAPI targetedFaction, CampaignFleetAPI fleet, PersonAPI person, SectorEntityToken hideout, String intelText, String bountyId) {
        this.baseReward = baseReward;
        this.repReward = repReward;
        this.intelText = intelText;
        this.offeringFaction = offeringFaction;
        this.targetedFaction = targetedFaction;
        this.fleet = fleet;
        this.person = person;
        this.hideout = hideout;
        this.bountyId = bountyId;
        this.flagship = fleet.getFlagship();
        this.shipType = flagship.getHullSpec().getHullNameWithDashClass() + " " + flagship.getHullSpec().getDesignation().toLowerCase();
        Misc.makeImportant(fleet, "pbe", 69420f);
    }

    public String getBountyId() {
        return bountyId;
    }

    @Override
    public FactionAPI getOfferingFaction() {
        return offeringFaction;
    }

    @Override
    public FactionAPI getTargetedFaction() {
        return targetedFaction;
    }

    @Override
    public CampaignFleetAPI getFleet() {
        return fleet;
    }

    @Override
    public PersonAPI getPerson() {
        return person;
    }

    @Override
    public SectorEntityToken getStartingPoint() {
        return hideout;
    }

    @Override
    public SectorEntityToken getEndingPoint() {
        return null;
    }

    @Override
    public String getIcon() {
        return person.getPortraitSprite();
    }

    @Override
    public String getTitle(BountyResult result) {
        if (isNotNull(result)) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                    return "Bounty Completed";
                case END_OTHER:
                case END_TIME:
                    return "Bounty Ended";
            }
        }
        return "High Value Bounty - " + person.getNameString();
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.BOSS;
    }

    @Override
    public int getBaseReward() {
        return baseReward;
    }

    public float getRepReward() {
        return repReward;
    }

    @Override
    public int getLevel() {
        return 69;
    }

    @Override
    public void addBulletPoints(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color bulletColor = baseBountyIntel.getBulletColorForMode(mode);
        float initPad = (mode == ListInfoMode.IN_DESC) ? 10f : 3f;

        BountyResult result = baseBountyIntel.getResult();
        baseBountyIntel.bullet(info);

        boolean isUpdate = baseBountyIntel.getListInfoParam() != null;

        if (result == null) {
            if (mode == ListInfoMode.IN_DESC) {
                info.addPara("%s reward on completion", initPad, bulletColor, highlightColor, Misc.getDGSCredits(baseReward));
                info.addPara("This bounty will not expire", 3f);
            } else {
                info.addPara("Offered by: " + offeringFaction.getDisplayName(), initPad, bulletColor,
                        offeringFaction.getBaseUIColor(), offeringFaction.getDisplayName());
                info.addPara("Target: " + person.getNameString(), 0f, bulletColor,
                        person.getFaction().getBaseUIColor(), person.getNameString());
                info.addPara("%s reward", 0f, bulletColor,
                        highlightColor, Misc.getDGSCredits(baseReward));

            }
        } else {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    String payout = Misc.getDGSCredits(baseReward);
                    info.addPara("%s received", initPad, bulletColor, highlightColor, payout);
                    CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, offeringFaction, null,
                            null, null, info, bulletColor, isUpdate, 3f);
                    break;
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                case END_TIME:
                case END_OTHER:
                    info.addPara("The bounty was killed by someone else...", initPad);
                    break;
            }
        }

        baseBountyIntel.unindent(info);
    }

    @Override
    public void createSmallDescription(BaseBountyIntel baseBountyIntel, TooltipMakerAPI info, float width, float height) {
        BountyResult result = baseBountyIntel.getResult();
        float opad = 10f;
        info.addImage(person.getPortraitSprite(), width, 128f, opad);
        info.addPara(intelText, opad);
        if (isNotNull(result)) {
            if (result.type == BountyResultType.END_PLAYER_BOUNTY)
                info.addPara("Confirming the successful elimination of " + person.getNameString(), opad, person.getFaction().getBaseUIColor(), person.getNameString());
            else info.addPara("This mission is no longer on offer.", opad);
        }

        addBulletPoints(baseBountyIntel, info, ListInfoMode.IN_DESC);

        if (isNull(result)) {
            if (isNotNull(hideout)) {
                SectorEntityToken fake = hideout.getContainingLocation().createToken(0.0F, 0.0F);
                fake.setOrbit(Global.getFactory().createCircularOrbit(hideout, 0.0F, 1000.0F, 100.0F));
                String loc = BreadcrumbSpecial.getLocatedString(fake);
                loc = loc.replaceAll("orbiting", "hiding out near");
                loc = loc.replaceAll("located in", "hiding out in");
                String sheIs = "She is";
                if (this.person.getGender() == FullName.Gender.MALE) {
                    sheIs = "He is";
                }

                info.addPara(sheIs + " rumored to be " + loc + ".", opad);
            }
            info.addPara(this.getTargetDesc() + " The bounty posting also contains intel on some of the ships under " + person.getHisOrHer() + " command.",
                    opad,
                    targetedFaction.getBaseUIColor(),
                    targetedFaction.getRank(person.getRankId()) + " " + person.getName().getFullName(), fleetDesc, flagship.getShipName(), shipType);
            info.addSectionHeading("Fleet Intel", offeringFaction.getBaseUIColor(), offeringFaction.getDarkUIColor(), Alignment.MID, opad);
            DescriptionUtils.createShipListForIntel(info, width, opad, fleet, 7, 3, true);
        }
    }

    public SectorEntityToken getHideout() {
        return hideout;
    }

    private String getTargetDesc() {
        int fleetSize = fleet.getNumShips();

        if (person == null) {
            log.error("tried to getTargetDesc but person was null");
            return "SOMETHING HAS GONE TERRIBLY WRONG";
        }
        if (person.getStats() == null) {
            log.error("tried to getTargetDesc but person.getStats() was null");
            return "SOMETHING HAS GONE TERRIBLY WRONG";
        }

        String heOrShe = "he";
        String hisOrHer = "his";
        if (person.isFemale()) {
            heOrShe = "she";
            hisOrHer = "her";
        }

        int personLevel = person.getStats().getLevel();
        if (personLevel <= 4) {
            levelDesc = "an unremarkable officer";
        } else if (personLevel <= 7) {
            levelDesc = "a capable officer";
        } else if (personLevel <= 11) {
            levelDesc = "a highly capable officer";
        } else {
            levelDesc = "an exceptionally capable officer";
        }

        List<MutableCharacterStatsAPI.SkillLevelAPI> knownSkills = fleet.getCommander().getStats().getSkillsCopy();
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();

        for (MutableCharacterStatsAPI.SkillLevelAPI skill : knownSkills) {
            String skillName = skill.getSkill().getId();
            switch (skillName) {
                case Skills.WEAPON_DRILLS:
                    picker.add("a great number of illegal weapon modifications");
                    break;
                case Skills.AUXILIARY_SUPPORT:
                    picker.add("armed-to-the-teeth support ships");
                    break;
                case Skills.COORDINATED_MANEUVERS:
                    picker.add("a high effectiveness in coordinating the maneuvers of ships during combat");
                    break;
                case Skills.WOLFPACK_TACTICS:
                    picker.add("highly coordinated frigate attacks");
                    break;
                case Skills.CREW_TRAINING:
                    picker.add("a very courageous crew");
                    break;
                case Skills.CARRIER_GROUP:
                    picker.add("a noteworthy level of skill in running carrier operations");
                    break;
                case Skills.OFFICER_TRAINING:
                    picker.add("having extremely skilled subordinates");
                    break;
                case Skills.OFFICER_MANAGEMENT:
                    picker.add("having a high number of skilled subordinates");
                    break;
                case Skills.NAVIGATION:
                    picker.add("having highly skilled navigators");
                    break;
                case Skills.SENSORS:
                    picker.add("having overclocked sensory equipment");
                    break;
                case Skills.ELECTRONIC_WARFARE:
                    picker.add("being proficient in electronic warfare");
                    break;
                case Skills.FIGHTER_UPLINK:
                    picker.add("removing engine-safety-protocols from fighters");
                    break;
                case Skills.FLUX_REGULATION:
                    picker.add("using overclocked flux coils");
                    break;
                case Skills.PHASE_CORPS:
                    picker.add("using experimental phase coils");
                    break;
                case Skills.FIELD_REPAIRS:
                    picker.add("having highly skilled mechanics");
                    break;
                case Skills.DERELICT_CONTINGENT:
                    picker.add("using military-grade duct tape");
                    break;
            }
        }

        Random random = new Random(person.getId().hashCode() * 1337L);
        picker.setRandom(random);

        skillDesc = picker.isEmpty() ? "nothing, really" : picker.pick();
        if (levelDesc.contains("unremarkable")) {
            levelDesc = "an otherwise unremarkable officer";
        }

        if (fleetSize <= 15) {
            fleetDesc = "small fleet";
        } else if (fleetSize <= 25) {
            fleetDesc = "medium-sized fleet";
        } else if (fleetSize <= 35) {
            fleetDesc = "large fleet";
        } else if (fleetSize <= 45) {
            fleetDesc = "very large fleet";
        } else if (fleetSize <= 55) {
            fleetDesc = "gigantic fleet";
        } else if (fleetSize <= 65) {
            fleetDesc = "freaking armada";
        } else {
            fleetDesc = "enough ships to push Jangala into the sun";
        }

        String targetDesc = String.format("%s is in command of a %s and personally commands the %s, a %s, as %s flagship.",
                targetedFaction.getRank(person.getRankId()) + " " + person.getName().getFullName(), fleetDesc, flagship.getShipName(), shipType, hisOrHer);

        targetDesc += String.format(" %s is %s known for %s.", Misc.ucFirst(heOrShe), levelDesc, skillDesc);

        return targetDesc;
    }
}