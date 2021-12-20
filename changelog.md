**BountiesExpanded** - *Version 0.4.2* - Changelog
```
0.4.2
    FIXED
        - Retrieval: fixed crash when abandoning the mission
        - Deserter and WarCriminal fleets losing their brain when the mission expires
    CHANGED
        - Replaced the mission difficulty assessment with a real threat assessment
        - WarCriminal: Destruction missions now require to destroy the whole fleet
        - WarCriminal: rebalanced payouts
0.4.1B
    FIXED
        - War Criminal: fixed crash when generating patrol descriptions
        - War Criminal: fixed crash when generating intel after finishing non Retrieval missions
        - War Criminal: fixed crash when finishing Destruction missions
    CHANGED
        - Pirate Bounties should now spawn more often
0.4.1A
    FIXED
        - Skirmish: Fixed rewards getting multiplied by 100
0.4.1
    FIXED
        - Crash when Vayras Sector and VS Bounty Removal was active (used an old version of VS..)
    ADDED
        - On a new game, 3 Pirate bounties will be generated (like in vanilla)
    CHANGED
        - War Criminal: Increased payout for Obliteration type
        - War Criminal: Bounties now spawn in non-hostile systems and will patrol there
0.4.0
    FIXED
        - Skirmish: 0 payout when friendly fleets join the battle
        - All: Ships will now spawn with DMods based on their factions highest production quality and the difficulty of the the bounty
    ADDED
        - War Criminal Hunt
          New Faction vs Faction bounty with 4 different goals
          -> Assassination - Eliminate the fleetcommander
          -> Destruction - Eliminate the fleetcommander (Flagship won't drop)
          -> Obliteration - Destroy the enemy fleet, no survivors, no recovery
          -> Retrieval - Capture the enemy flagship and bring it back to the mission contractor (recovery guaranteed)
          (Destruction and Obliteration will have additional rewards later on)
        - Pirate Bounty
          Replaces vanilla Pirate bounties
        - Deserter Bounty
          Replaces vanilla Deserter bounties
        - Rare Flagships
          Bounties now have a chance to spawn with a rare flagship (fleet description will give you a hint about that)
    REMOVED
        - High Value Bounties
          Passed the burden on to MagicBounties
    CHANGED
        - Bounties will now have a minimum FP for spawning
          Skirmish 100 FP, Assassination 50 FP, WarCriminal 50 FP, Pirate 30 FP, Deserter 40 FP
0.3.3C
    FIXED
        - Skirmish: corrected wrong reputation change display for targeted faction
        - Skirmish: corrected wrong offered payout display in summary
        - HVB: no more crashes when there are no mods with HVB data loaded
0.3.3B
    FIXED
        - HVB: fixed crash when HVBs are disabled
        - Skirmish: wrong base payout
    ADDED
        - Polished the new Skirmish description a bit
0.3.3A
    FIXED
        - HVB: fixed crash when fleetVariantID is empty in .csv
        - Assassination: fixed crash when fleet creation failed
0.3.3
    FIXED
        - HVB: no longer engage other fleets (Nexus..) and getting killed
        - HVB: fixed ship recovery for real now
        - HVB: escort ships are getting their SMods back again
        - Skirmish: joined fleets won't count towards kills anymore
        - Skirmish: fleets now ignore other fleets (can still be engaged by others)
        - Assassination: fixed rare NPE in regards empty source market
        - FleetCreation: fixed NPE when empty fleet gets generated

    OTHER
        - SMods: a variant won't get upgraded if it already has built-in SMods
0.3.2
    FIXED
        - Skirmish: Fleet duplication bug
        - HVB: description fixed
        - HVB: double recovery fixed
        - HVB: recovery will now use AutoRecover setting and won't add unboardable ships anymore
        - HVB: completed bounties will now get removed after 3 days from intel
        - Fleet order display fixed
        - even more bugs that i forget to document and nobody noticed anyway

    BALANCE
        - Tweaked fleet composition and strenght
        - Payout tweaks
        - Fleets of all bounties will now have a chance to get SMods
        - Skirmish: bounties will now only spawn once per faction. No more stacked bounties against a single faction.
        - Skirmish: bounties payment and rep gain scale with offering factions reputation to the player
        - Skirmish: factions prefer to give out bounties to players with positiv rep

    ADDED
        - Fancy new summary for Skirmish bounties!
0.3.1
    FIXED   - Rare error when removing active VS-HVBs
            - Wrong setting for max HVBs
            - Tweaked bounty rewards slightly
0.3.0
    BALANCE - Fleet strenght is now based on player FP. The bigger your fleet, the bigger the target.
            - New fleet generation method to avoid late game capital bloat
            - Assassination bounties will now send an update when entering hyperspace and marked as important
            - Assassination bounties base payment reduced (-50%), bonus payment increased (+100%)
    ADDED   - New icons for Assassination (thanks to Selki!) and Skirmish bounties
            - High Value Bounties! They will replace VS ones if installed (can be deactivated in settings)
            - Console Command support!
    OTHER   - Many small fixes everywhere
            - no more uninstall-option. HVB made that impossible :-/
0.2.2
    FIXED   - Ignoring markets in hyperspace to avoid NPE and other stupid things (looking at you, LTA!)
            - Fixed wrong hyperspace destination for Assassination fleets
    OTHER   - Added more Debug-Infos
0.2.1
    BALANCE - Optimised Assassination travel assignments
            - Skirmish fleets will no longer spawn around a planet that has already a bounty active
    ADDED   - Difficulty based hints about Assassination targets hideout
    FIXED   - Uninstaller will no longer crash when a bounty type never got activated on that save
    OTHER   - Code cleanup and optimisations

0.2.0
    BALANCE - adjusted Skirmish fleet generation
    ADDED   - new bounty type: Assassination
            - VersionChecker support
    OTHER   - code refactoring and restructuring (sorry!)

0.1.3
    FIXED   - more NPE fixes that got reported
            - bounties won't spawn at player worlds anymore
    BALANCE - scaled fleet inflation down a notch
    ADDED   - Uninstall-Setting added
          Set to true -> load game -> save game -> disable mod

0.1.2
    fixed possible NPE if mod-faction-creators messed up and added NULL to the participating faction list...

0.1.1
    bundled BountyLib since the eggdog doesn't want any more libs
```