package com.lilithsthrone.controller.eventListeners;

import java.util.Map.Entry;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import com.lilithsthrone.controller.TooltipUpdateThread;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.PlayerCharacter;
import com.lilithsthrone.game.character.attributes.ArousalLevel;
import com.lilithsthrone.game.character.attributes.Attribute;
import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.attributes.FitnessLevel;
import com.lilithsthrone.game.character.attributes.IntelligenceLevel;
import com.lilithsthrone.game.character.attributes.StrengthLevel;
import com.lilithsthrone.game.character.body.CoverableArea;
import com.lilithsthrone.game.character.body.types.AntennaType;
import com.lilithsthrone.game.character.body.types.BodyCoveringType;
import com.lilithsthrone.game.character.body.types.HornType;
import com.lilithsthrone.game.character.body.types.PenisType;
import com.lilithsthrone.game.character.body.types.TailType;
import com.lilithsthrone.game.character.body.types.VaginaType;
import com.lilithsthrone.game.character.body.types.WingType;
import com.lilithsthrone.game.character.body.valueEnums.Femininity;
import com.lilithsthrone.game.character.effects.Fetish;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.effects.PerkInterface;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.game.combat.Combat;
import com.lilithsthrone.game.combat.SpecialAttack;
import com.lilithsthrone.game.combat.Spell;
import com.lilithsthrone.game.dialogue.utils.CharactersPresentDialogue;
import com.lilithsthrone.game.dialogue.utils.PhoneDialogue;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Colour;
import com.lilithsthrone.utils.Util;

/**
 * @since 0.1.0
 * @version 0.1.83
 * @author Innoxia
 */
public class TooltipInformationEventListener implements EventListener {
	private String title, description;
	private boolean extraAttributes = false, opponentExtraAttributes = false, weather = false, protection = false, tattoo = false, copyInformation=false;
	private GameCharacter owner;
	private StatusEffect statusEffect;
	private PerkInterface perk, levelUpPerk;
	private Fetish fetish;
	private SpecialAttack specialAttack;
	private Spell spell;
	private int spellLevel;
	private Attribute attribute;
	
	private static StringBuilder tooltipSB;
	static {
		tooltipSB = new StringBuilder();
	}

	/*
	 * I have to manually set tooltip height, as all the JavaFX methods for getting document height are completely broken. (Don't bother wasting any more time with JavaFX crap.) R-Rude...
	 */
	
	@Override
	public void handleEvent(Event event) {
		Main.mainController.setTooltipSize(360, 180);
		Main.mainController.setTooltipContent("");

		if (statusEffect != null) {
			
			int yIncrease = (statusEffect.getModifiersAsStringList(owner).size() > 4 ? statusEffect.getModifiersAsStringList(owner).size() - 4 : 0)
								+ (owner.hasStatusEffect(statusEffect)?(owner.getStatusEffectDuration(statusEffect) == -1 ? 0 : 2):0);

			Main.mainController.setTooltipSize(360, 284 + (yIncrease * 20));

			// Title:
			tooltipSB.setLength(0);
			tooltipSB.append("<body>"
					+ "<div class='title'>" + Util.capitaliseSentence(statusEffect.getName(owner)) + "</div>");

			// Attribute modifiers:
			tooltipSB.append("<div class='subTitle-picture'>");
				if (statusEffect.getModifiersAsStringList(owner).size() != 0) {
					tooltipSB.append("<b style='color:" + Colour.STATUS_EFFECT.toWebHexString() + ";'>Status Effect</b>");
					for (String s : statusEffect.getModifiersAsStringList(owner))
						tooltipSB.append("</br>" + s);
				} else {
					tooltipSB.append("<b style='color:" + Colour.STATUS_EFFECT.toWebHexString() + ";'>Status Effect</b>" + "</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>No bonuses</span>");
				}
			tooltipSB.append("</div>");

			// Picture:
			tooltipSB.append("<div class='picture'>"
								+ statusEffect.getSVGString(owner)
							+ "</div>"
							+ "<div class='description'>"
								+ statusEffect.getDescription(owner)
							+ "</div>");

			if(owner.hasStatusEffect(statusEffect))
				if (owner.getStatusEffectDuration(statusEffect) != -1) {
					if (statusEffect.isCombatEffect()) {
						tooltipSB.append("<div class='subTitle'><b>Turns remaining: " + owner.getStatusEffectDuration(statusEffect) + "</b></div>");
					} else {
						int timerHeight = (int) ((owner.getStatusEffectDuration(statusEffect)/(60*6f))*100);

						Colour timerColour = Colour.STATUS_EFFECT_TIME_HIGH;
						
						if(timerHeight>100) {
							timerHeight=100;
							timerColour = Colour.STATUS_EFFECT_TIME_OVERFLOW;
						} else if(timerHeight<15) {
							timerColour = Colour.STATUS_EFFECT_TIME_LOW;
						} else if (timerHeight<50) {
							timerColour = Colour.STATUS_EFFECT_TIME_MEDIUM;
						}
						
						tooltipSB.append("<div class='subTitle'><b>Time remaining: "
								+ "<b style='color:"+timerColour.toWebHexString()+";'>" + (owner.getStatusEffectDuration(statusEffect) / 60) + ":" + String.format("%02d", (owner.getStatusEffectDuration(statusEffect) % 60)) + "</b>"
								+ "</div>");
						//STATUS_EFFECT_TIME_OVERFLOW
					}
				}
			
			tooltipSB.append("</body>");
			
			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (perk != null) { // Perks:
			
			if(perk instanceof Perk) {
			
				int yIncrease = (perk.getModifiersAsStringList().size() > 4 ? perk.getModifiersAsStringList().size() - 4 : 0);
	
				Main.mainController.setTooltipSize(360, 288 + (yIncrease * 20));
	
				// Title:
				tooltipSB.setLength(0);
				tooltipSB.append("<div class='title'>" + Util.capitaliseSentence(perk.getName(owner)) + "</div>");
				
				// Attribute modifiers:
				tooltipSB.append("<div class='subTitle-picture'>");
				if (perk.getModifiersAsStringList().size() != 0) {
					tooltipSB.append("<b style='color:" + Colour.PERK.toWebHexString() + ";'>Perk</b>");
					for (String s : perk.getModifiersAsStringList())
						tooltipSB.append("</br>" + s);
				} else
					tooltipSB.append("<b style='color:" + Colour.PERK.toWebHexString() + ";'>Perk</b>" + "</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>");
				tooltipSB.append("</div>");
	
				// Picture:
				tooltipSB.append("<div class='picture'>" + perk.getSVGString() + "</div>");
	
				// Description:
				tooltipSB.append("<div class='description'>" + perk.getDescription(owner) + "</div>");
	
				Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));
				
			} else if(perk instanceof Fetish) {
				int yIncrease = (perk.getModifiersAsStringList().size() > 4 ? perk.getModifiersAsStringList().size() - 4 : 0);
				
				Main.mainController.setTooltipSize(360, 288 + (yIncrease * 20));
	
				// Title:
				tooltipSB.setLength(0);
				tooltipSB.append("<div class='title'>" + Util.capitaliseSentence(perk.getName(owner)) + " fetish</div>");
				
				// Attribute modifiers:
				tooltipSB.append("<div class='subTitle-picture'>");
				if (perk.getModifiersAsStringList().size() != 0) {
					tooltipSB.append("<b style='color:" + Colour.FETISH.toWebHexString() + ";'>Fetish</b>");
					for (String s : perk.getModifiersAsStringList())
						tooltipSB.append("</br>" + s);
				} else
					tooltipSB.append("<b style='color:" + Colour.FETISH.toWebHexString() + ";'>Fetish</b>" + "</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>");
				tooltipSB.append("</div>");
	
				// Picture:
				tooltipSB.append("<div class='picture'>" + perk.getSVGString() + "</div>");
	
				// Description:
				tooltipSB.append("<div class='description'>" + perk.getDescription(owner) + "</div>");
	
				Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));
			}

		} else if (levelUpPerk != null) { // Level Up Perk (same as Perk, but with requirements at top):

			int yIncrease = (levelUpPerk.getModifiersAsStringList().size() > 4 ? levelUpPerk.getModifiersAsStringList().size() - 4 : 0) + levelUpPerk.getPerkRequirements(Main.game.getPlayer(), PhoneDialogue.levelUpPerks).size();

			Main.mainController.setTooltipSize(360, 324 + (yIncrease * 20));

			// Title:
			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>" + Util.capitaliseSentence(levelUpPerk.getName(owner)) + "</div>");
			
			// Requirements:
			tooltipSB.append("<div class='subTitle'>Requirements");
			for (String s : levelUpPerk.getPerkRequirements(Main.game.getPlayer(), PhoneDialogue.levelUpPerks))
				tooltipSB.append("</br>" + s);
			tooltipSB.append("</div>");

			// Attribute modifiers:
			tooltipSB.append("<div class='subTitle-picture'>");
			if (levelUpPerk.getModifiersAsStringList().size() != 0) {
				tooltipSB.append("<b style='color:" + Colour.PERK.toWebHexString() + ";'>Perk</b>");
				for (String s : levelUpPerk.getModifiersAsStringList())
					tooltipSB.append("</br>" + s);
			} else
				tooltipSB.append("<b style='color:" + Colour.PERK.toWebHexString() + ";'>Perk</b>" + "</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>");
			tooltipSB.append("</div>");

			// Picture:
			tooltipSB.append("<div class='picture'>" + levelUpPerk.getSVGString() + "</div>");

			// Description:
			tooltipSB.append("<div class='description'>" + levelUpPerk.getDescription(Main.game.getPlayer()) + "</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (fetish != null) { // Fetishes:

			int yIncrease = (fetish.getModifiersAsStringList().size() > 4 ? fetish.getModifiersAsStringList().size() - 4 : 0) + fetish.getFetishesForAutomaticUnlock().size();

			Main.mainController.setTooltipSize(360, (fetish.getFetishesForAutomaticUnlock().size()==0?288:324) + (yIncrease * 20));

			// Title:
			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>" + Util.capitaliseSentence(fetish.getName(owner)) + " fetish</div>");
			
			// Requirements:
			if(fetish.getFetishesForAutomaticUnlock().size()>=1) {
				tooltipSB.append("<div class='subTitle'>Requirements");
				for (Fetish f : fetish.getFetishesForAutomaticUnlock())
					tooltipSB.append("</br>[style.boldArcane(" + Util.capitaliseSentence(f.getName(Main.game.getPlayer()))+")]");
				tooltipSB.append("</div>");
			}
			
			// Attribute modifiers:
			tooltipSB.append("<div class='subTitle-picture'>");
			if (fetish.getModifiersAsStringList().size() != 0) {
				tooltipSB.append("<b style='color:" + Colour.FETISH.toWebHexString() + ";'>Fetish</b>");
				for (String s : fetish.getModifiersAsStringList())
					tooltipSB.append("</br>" + s);
			} else
				tooltipSB.append("<b style='color:" + Colour.FETISH.toWebHexString() + ";'>Fetish</b>" + "</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>");
			tooltipSB.append("</div>");

			// Picture:
			tooltipSB.append("<div class='picture'>" + fetish.getSVGString() + "</div>");

			// Description:
			tooltipSB.append("<div class='description'>" + fetish.getDescription(owner) + "</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (specialAttack != null) { // Special attacks:

			int yIncrease = (specialAttack.getStatusEffects().size() > 2 ? specialAttack.getStatusEffects().size() - 2 : 0);

			Main.mainController.setTooltipSize(360, 324 + (yIncrease * 20));

			// Title:
			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>" + Util.capitaliseSentence(specialAttack.getName()) + "</div>");

			// Attribute modifiers:
			tooltipSB.append("<div class='subTitle-picture'>" + "<b style='color:" + Colour.SPECIAL_ATTACK.toWebHexString() + ";'>Special Attack</b></br>" + "<b>" + (specialAttack.getMinimumDamage(owner, null)) + "-"
					+ (specialAttack.getMaximumDamage(owner, null)) + "</b>" + " <b style='color:" + specialAttack.getDamageType().getMultiplierAttribute().getColour().toWebHexString() + ";'>" + specialAttack.getDamageType().getName()
					+ "</b> damage");

			tooltipSB.append("</br><b style='color:" + Colour.SPECIAL_ATTACK.toWebHexString() + ";'>Applies</b>");
			if (specialAttack.getStatusEffects().size() != 0) {
				for (Entry<StatusEffect, Integer> e : specialAttack.getStatusEffects().entrySet())
					tooltipSB.append("</br><b style='color:" + e.getKey().getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(e.getKey().getName(owner)) + "</b> for " + e.getValue() + " turn" + (e.getValue() > 1 ? "s" : ""));
			} else
				tooltipSB.append("</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>No effects</span>");
			tooltipSB.append("</div>");

			// Picture:
			tooltipSB.append("<div class='picture'>" + specialAttack.getSVGString() + "</div>");

			// Description & turns remaining:
			tooltipSB.append("<div class='description'>" + specialAttack.getDescription(owner) + "</div>");

			tooltipSB.append("<div class='subTitle'>"
					+ "<b style='color:" + Colour.GENERIC_BAD.toWebHexString() + ";'>Costs</b> <b>" + (specialAttack.getMinimumCost(owner)) + " - " + (specialAttack.getMaximumCost(owner)) + "</b>"
							+ " <b style='color:" + Colour.ATTRIBUTE_FITNESS.toWebHexString() + ";'>stamina</b>" + "</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (spell != null) { // Spells:

			int yIncrease = (spell.getStatusEffects().size() > 2 ? spell.getStatusEffects().size() - 2 : 0);

			Main.mainController.setTooltipSize(360, 324 + (yIncrease * 20));

			// Title:
			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>" + Util.capitaliseSentence(spell.getName()) + "</div>");

			// Attribute modifiers:
			tooltipSB.append("<div class='subTitle-picture'>" + "<b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Spell</b></br>");

			if (spell.isSelfCastSpell())
				tooltipSB.append("<b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>Beneficial</b> <b style='color:" + spell.getDamageType().getMultiplierAttribute().getColour().toWebHexString() + ";'>" + spell.getDamageType().getName()
						+ "</b> spell");
			else
				tooltipSB.append("<b>" + (spell.getMinimumDamage(owner, null, spellLevel)) + "-" + (spell.getMaximumDamage(owner, null, spellLevel)) + "</b>" + " <b style='color:"
						+ spell.getDamageType().getMultiplierAttribute().getColour().toWebHexString() + ";'>" + spell.getDamageType().getName() + "</b> damage");

			tooltipSB.append("</br><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Applies</b>");
			if (spell.getStatusEffects().size() != 0) {
				for (Entry<StatusEffect, Integer> e : spell.getStatusEffects().entrySet())
					tooltipSB.append("</br><b style='color:" + e.getKey().getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(e.getKey().getName(owner)) + "</b> for " + e.getValue() + " turn" + (e.getValue() > 1 ? "s" : ""));
			} else
				tooltipSB.append("</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>No effects</span>");

			tooltipSB.append("</div>");

			// Picture:
			tooltipSB.append("<div class='picture'>" + spell.getSVGString() + "</div>");

			// Description & turns remaining:
			tooltipSB.append("<div class='description'>" + spell.getDescription(owner, spellLevel) + "</div>");

			tooltipSB.append("<div class='subTitle'>"
					+ "<b style='color:" + Colour.GENERIC_BAD.toWebHexString() + ";'>Costs</b> <b>" + (spell.getMinimumCost(owner, spellLevel)) + " - " + (spell.getMaximumCost(owner, spellLevel)) + "</b>"
							+ " <b style='color:" + Colour.ATTRIBUTE_MANA.toWebHexString() + ";'>willpower</b>" + "</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (attribute != null) {
			
			if (attribute == Attribute.STRENGTH || attribute == Attribute.INTELLIGENCE || attribute == Attribute.FITNESS || attribute == Attribute.CORRUPTION || attribute == Attribute.AROUSAL) {
				StatusEffect currentAttributeStatusEffect=null;
				int minimumLevelValue=0, maximumLevelValue=0;
				
				if(attribute == Attribute.STRENGTH) {
					currentAttributeStatusEffect = StrengthLevel.getStrengthLevelFromValue(owner.getAttributeValue(Attribute.STRENGTH)).getRelatedStatusEffect();
					minimumLevelValue = StrengthLevel.getStrengthLevelFromValue(owner.getAttributeValue(Attribute.STRENGTH)).getMinimumValue();
					maximumLevelValue = StrengthLevel.getStrengthLevelFromValue(owner.getAttributeValue(Attribute.STRENGTH)).getMaximumValue();
					
				} else if(attribute == Attribute.INTELLIGENCE) {
					currentAttributeStatusEffect = IntelligenceLevel.getIntelligenceLevelFromValue(owner.getAttributeValue(Attribute.INTELLIGENCE)).getRelatedStatusEffect();
					minimumLevelValue = IntelligenceLevel.getIntelligenceLevelFromValue(owner.getAttributeValue(Attribute.INTELLIGENCE)).getMinimumValue();
					maximumLevelValue = IntelligenceLevel.getIntelligenceLevelFromValue(owner.getAttributeValue(Attribute.INTELLIGENCE)).getMaximumValue();
					
				} else if(attribute == Attribute.FITNESS) {
					currentAttributeStatusEffect = FitnessLevel.getFitnessLevelFromValue(owner.getAttributeValue(Attribute.FITNESS)).getRelatedStatusEffect();
					minimumLevelValue = FitnessLevel.getFitnessLevelFromValue(owner.getAttributeValue(Attribute.FITNESS)).getMinimumValue();
					maximumLevelValue = FitnessLevel.getFitnessLevelFromValue(owner.getAttributeValue(Attribute.FITNESS)).getMaximumValue();
					
				} else if(attribute == Attribute.CORRUPTION) {
					currentAttributeStatusEffect = CorruptionLevel.getCorruptionLevelFromValue(owner.getAttributeValue(Attribute.CORRUPTION)).getRelatedStatusEffect();
					minimumLevelValue = CorruptionLevel.getCorruptionLevelFromValue(owner.getAttributeValue(Attribute.CORRUPTION)).getMinimumValue();
					maximumLevelValue = CorruptionLevel.getCorruptionLevelFromValue(owner.getAttributeValue(Attribute.CORRUPTION)).getMaximumValue();
					
				} else {
					currentAttributeStatusEffect = ArousalLevel.getArousalLevelFromValue(owner.getAttributeValue(Attribute.AROUSAL)).getRelatedStatusEffect();
					minimumLevelValue = ArousalLevel.getArousalLevelFromValue(owner.getAttributeValue(Attribute.AROUSAL)).getMinimumValue();
					maximumLevelValue = ArousalLevel.getArousalLevelFromValue(owner.getAttributeValue(Attribute.AROUSAL)).getMaximumValue();
				}
				
				
				int yIncrease = (currentAttributeStatusEffect.getModifiersAsStringList(owner).size() > 4 ? currentAttributeStatusEffect.getModifiersAsStringList(owner).size() - 4 : 0)
						+ (owner.hasStatusEffect(currentAttributeStatusEffect)?(owner.getStatusEffectDuration(currentAttributeStatusEffect) == -1 ? 0 : 2):0);

				Main.mainController.setTooltipSize(360, 460 + (yIncrease * 20));
				
				tooltipSB.setLength(0);
				tooltipSB.append("<div class='title' style='color:" + attribute.getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(attribute.getName()) + "</div>"

						+ "<div class='subTitle-third'>" + "<b style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>Core</b></br>"
						+ (owner.getBaseAttributeValue(attribute) > 0 ? "<span style='color: " + Colour.GENERIC_EXCELLENT.getShades()[1] + ";'>" : "<span>") + String.format("%.2f", owner.getBaseAttributeValue(attribute)) + "</span>" + "</div>"
						
						+ "<div class='subTitle-third'>" + "<b style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>Bonus</b></br>"
						+ ((owner.getBonusAttributeValue(attribute)) > 0 ? "<span style='color: " + Colour.GENERIC_GOOD.getShades()[1] + ";'>"
								: ((owner.getBonusAttributeValue(attribute)) == 0 ? "<span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>" : "<span style='color: " + Colour.GENERIC_BAD.getShades()[1] + ";'>"))
						+ String.format("%.2f", owner.getBonusAttributeValue(attribute))+ "</span>" + "</div>"
						
						+ "<div class='subTitle-third'>" + "<b style='color:" + attribute.getColour().toWebHexString() + ";'>Total</b></br>" + String.format("%.2f", owner.getAttributeValue(attribute))
						+ "</span>" + "</div>");
				
				tooltipSB.append("<div class='description-half'>" + attribute.getDescription(owner) + "</div>");
				
				// Related status effect:
				tooltipSB.append("<div class='title'>"
												+ "<span style='color:"+currentAttributeStatusEffect.getColour().toWebHexString()+";'>"
												+ currentAttributeStatusEffect.getName(owner)
												+"</span> ("+minimumLevelValue
												+"-"
												+ maximumLevelValue
												+")"
												+ "</div>");
			
				// Attribute modifiers:
				tooltipSB.append("<div class='subTitle-picture'>");
				if (currentAttributeStatusEffect.getModifiersAsStringList(owner).size() != 0) {
					tooltipSB.append("<b style='color:" + Colour.STATUS_EFFECT.toWebHexString() + ";'>Status Effect</b>");
					for (String s : currentAttributeStatusEffect.getModifiersAsStringList(owner))
						tooltipSB.append("</br>" + s);
				} else
					tooltipSB.append("<b style='color:" + Colour.STATUS_EFFECT.toWebHexString() + ";'>Status Effect</b>" + "</br><span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>No bonuses</span>");
				tooltipSB.append("</div>");
			
				// Picture:
				tooltipSB.append("<div class='picture'>" + currentAttributeStatusEffect.getSVGString(owner) + "</div>");
			
				// Description & turns remaining:
				tooltipSB.append("<div class='description'>" + currentAttributeStatusEffect.getDescription(owner) + "</div>");

				Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

			} else if (attribute == Attribute.EXPERIENCE) {
				// Special tooltip for experience/transformation combo:

				Main.mainController.setTooltipSize(420, 560);

				tooltipSB.setLength(0);
				tooltipSB.append("<div class='title' style='color:" + owner.getRace().getColour().toWebHexString() + ";'>"
						+(owner.getRaceStage().getName()!=""?"<b style='color:"+owner.getRaceStage().getColour().toWebHexString()+";'>" + Util.capitaliseSentence(owner.getRaceStage().getName())+"</b> ":"")
						+ "<b style='color:"+owner.getRace().getColour().toWebHexString()+";'>"
						+ (owner.isFeminine()?Util.capitaliseSentence(owner.getRace().getSingularFemaleName()):Util.capitaliseSentence(owner.getRace().getSingularMaleName()))
						+ "</b>"
						+ "</div>");
				
				if(owner.isPlayer()) {
					tooltipSB.append("<div class='subTitle'>" + "Level " + owner.getLevel() + " <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>|</span> " + ((PlayerCharacter)owner).getExperience() + " / "
							+ (10 * owner.getLevel()) + " xp" + "</div>");
				} else {
					tooltipSB.append("<div class='subTitle'>" + "Level " + owner.getLevel() + "</div>");
				}

				// GREATER:
				tooltipSB.append(getBodyPartDiv("Face", owner.getFaceRace(), owner.getFaceType().getBodyCoveringType()));
				tooltipSB.append(getBodyPartDiv("Body", owner.getSkinRace(), owner.getSkinType().getBodyCoveringType()));
				

				// LESSER:
				tooltipSB.append(getBodyPartDiv("Arms", owner.getArmRace(), owner.getArmType().getBodyCoveringType()));
				tooltipSB.append(getBodyPartDiv("Legs", owner.getLegRace(), owner.getLegType().getBodyCoveringType()));
				
				// PARTIAL:
				tooltipSB.append(getBodyPartDiv("Hair", owner.getHairRace(), owner.getHairType().getBodyCoveringType()));
				tooltipSB.append(getBodyPartDiv("Eyes", owner.getEyeRace(), owner.getEyeType().getBodyCoveringType()));
				tooltipSB.append(getBodyPartDiv("Ears", owner.getEarRace(), owner.getEarType().getBodyCoveringType()));
				tooltipSB.append(getBodyPartDiv("Tongue", owner.getTongueRace(), owner.getTongueType().getBodyCoveringType()));
				if (owner.getHornType() != HornType.NONE) {
					tooltipSB.append(getBodyPartDiv("Horns", owner.getHornRace(), owner.getHornType().getBodyCoveringType()));
				} else {
					tooltipSB.append("<div class='subTitle-half body'>" + "Horns - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>" + "</div>");
				}
				if (owner.getAntennaType() != AntennaType.NONE) {
					tooltipSB.append(getBodyPartDiv("Antennae", owner.getAntennaRace(), owner.getAntennaType().getBodyCoveringType()));
				} else {
					tooltipSB.append("<div class='subTitle-half body'>" + "Antenna - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>" + "</div>");
				}
				if (owner.getWingType() != WingType.NONE) {
					tooltipSB.append(getBodyPartDiv("Wings", owner.getWingRace(), owner.getWingType().getBodyCoveringType()));
				} else {
					tooltipSB.append("<div class='subTitle-half body'>" + "Wings - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>" + "</div>");
				}
				if (owner.getTailType() != TailType.NONE) {
					tooltipSB.append(getBodyPartDiv("Tail", owner.getTailRace(), owner.getTailType().getBodyCoveringType()));
				} else {
					tooltipSB.append("<div class='subTitle-half body'>" + "Tail - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>" + "</div>");
				}
				
				// SEXUAL:
				if(!owner.isPlayer() && !owner.getPlayerKnowsAreasMap().get(CoverableArea.VAGINA)) {
					tooltipSB.append("<div class='subTitle-half body'>Vagina - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>Unknown!</span></div>");
				} else {
					if (owner.getVaginaType() != VaginaType.NONE) {
						tooltipSB.append(getBodyPartDiv("Vagina", owner.getVaginaRace(), owner.getVaginaType().getBodyCoveringType()));
					} else {
						tooltipSB.append("<div class='subTitle-half body'>" + "Vagina - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>" + "</div>");
					}
				}
				
				if(!owner.isPlayer() && !owner.getPlayerKnowsAreasMap().get(CoverableArea.PENIS)) {
					tooltipSB.append("<div class='subTitle-half body'>Penis - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>Unknown!</span></div>");
				} else {
					if (owner.getPenisType() != PenisType.NONE) {
						tooltipSB.append(getBodyPartDiv("Penis", owner.getPenisRace(), owner.getPenisType().getBodyCoveringType()));
					} else {
						tooltipSB.append("<div class='subTitle-half body'>" + "Penis - <span style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>None</span>" + "</div>");
					}
				}
				tooltipSB.append(getBodyPartDiv("Ass", owner.getAssRace(), owner.getAssType().getBodyCoveringType()));
				tooltipSB.append(getBodyPartDiv("Breasts", owner.getBreastRace(), owner.getBreastType().getBodyCoveringType()));
				
				Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

			} else {
				Main.mainController.setTooltipSize(360, 234);

				Main.mainController.setTooltipContent(UtilText.parse(
						"<div class='title' style='color:" + attribute.getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(attribute.getName()) + "</div>"

						+ "<div class='subTitle-third'>"
						+ "<b style='color:"
						+ Colour.TEXT_GREY.toWebHexString()
						+ ";'>Core</b></br>"
						+ (owner.getBaseAttributeValue(attribute) > 0 ? "<span style='color: "
								+ Colour.GENERIC_EXCELLENT.getShades()[1]
								+ ";'>" : "<span>")
						+ owner.getBaseAttributeValue(attribute)
						+ "</span>"
						+ "</div>"
						+ "<div class='subTitle-third'>"
						+ "<b style='color:"
						+ Colour.TEXT_GREY.toWebHexString()
						+ ";'>Bonus</b></br>"
						+ ((owner.getBonusAttributeValue(attribute)) > 0 ? "<span style='color: "
								+ Colour.GENERIC_GOOD.getShades()[1]
								+ ";'>"
								: ((owner.getBonusAttributeValue(attribute)) == 0 ? "<span style='color:"
										+ Colour.TEXT_GREY.toWebHexString()
										+ ";'>"
										: "<span style='color: "
												+ Colour.GENERIC_BAD.getShades()[1]
												+ ";'>"))
						+ owner.getBonusAttributeValue(attribute)
						+ "</span>"
						+ "</div>"
						+ "<div class='subTitle-third'>"
						+ "<b style='color:"
						+ attribute.getColour().toWebHexString()
						+ ";'>Total</b></br>"
						+ owner.getAttributeValue(attribute)
						+ "</span>"
						+ "</div>"

						+ "<div class='description'>" + attribute.getDescription(owner) + "</div>"));
				
			}

		} else if (extraAttributes) {

			Main.mainController.setTooltipSize(360, 600);

			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title' style='color:" + Femininity.valueOf(Main.game.getPlayer().getFemininityValue()).getColour().toWebHexString() + ";'>"
					+ (owner.getName().length() == 0 ? (owner.getFemininityValue() <= Femininity.MASCULINE.getMaximumFemininity() ? "Hero" : "Heroine") : owner.getName()) + "</div>"

					+ extraAttributeBonus(owner, Attribute.CRITICAL_CHANCE)
					+ extraAttributeBonus(owner, Attribute.CRITICAL_DAMAGE)

					// Header:
					+ "<div class='subTitle-third combatValue'>" + "Type" + "</div>" + "<div class='subTitle-third combatValue'>" + "Damage" + "</div>" + "<div class='subTitle-third combatValue'>" + "Resist" + "</div>"

					// Values:
					+ extraAttributeTableRow(owner, "Melee", Attribute.DAMAGE_ATTACK, Attribute.RESISTANCE_ATTACK)
					+ extraAttributeTableRow(owner, "Spell", Attribute.DAMAGE_SPELLS, Attribute.RESISTANCE_SPELLS)

					+ extraAttributeTableRow(owner, "Physical", Attribute.DAMAGE_PHYSICAL, Attribute.RESISTANCE_PHYSICAL)
					+ extraAttributeTableRow(owner, "Fire", Attribute.DAMAGE_FIRE, Attribute.RESISTANCE_FIRE)
					+ extraAttributeTableRow(owner, "Cold", Attribute.DAMAGE_ICE, Attribute.RESISTANCE_ICE)
					+ extraAttributeTableRow(owner, "Poison", Attribute.DAMAGE_POISON, Attribute.RESISTANCE_POISON)
					+ extraAttributeTableRow(owner, "Willpower", Attribute.DAMAGE_MANA, Attribute.RESISTANCE_MANA)
					+ extraAttributeTableRow(owner, "Stamina", Attribute.DAMAGE_STAMINA, Attribute.RESISTANCE_STAMINA)

					+ extraAttributeTableRow(owner, "Pure", Attribute.DAMAGE_PURE, Attribute.RESISTANCE_PURE)
					
					+ extraAttributeBonus(owner, Attribute.FERTILITY)
					+ extraAttributeBonus(owner, Attribute.VIRILITY)
					
					+ extraAttributeBonus(owner, Attribute.SPELL_COST_MODIFIER));

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (opponentExtraAttributes) {

			Main.mainController.setTooltipSize(360, 480);

			tooltipSB.setLength(0);
			GameCharacter target = null;
			if(Main.game.isInCombat()) {
				target = Combat.getOpponent();
			} else {
				target = CharactersPresentDialogue.characterViewed;
			}
			tooltipSB.append("<div class='title' style='color:" + Femininity.valueOf(target.getFemininityValue()).getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(target.getName()) + "</div>");

			tooltipSB.append(
					extraAttributeBonus(target, Attribute.CRITICAL_CHANCE)
					+ extraAttributeBonus(target, Attribute.CRITICAL_DAMAGE)

					// Header:
					+ "<div class='subTitle-third combatValue'>" + "Type" + "</div>" + "<div class='subTitle-third combatValue'>" + "&#8224 Damage &#8224" + "</div>" + "<div class='subTitle-third combatValue'>" + "&#8225 Resist &#8225" + "</div>"

					// Values:
					+ extraAttributeTableRow(target, "Melee", Attribute.DAMAGE_ATTACK, null)
					+ extraAttributeTableRow(target, "Spell", Attribute.DAMAGE_SPELLS, null)

					+ extraAttributeTableRow(target, "Physical", Attribute.DAMAGE_PHYSICAL, Attribute.RESISTANCE_PHYSICAL)
					+ extraAttributeTableRow(target, "Fire", Attribute.DAMAGE_FIRE, Attribute.RESISTANCE_FIRE) + extraAttributeTableRow(target, "Cold", Attribute.DAMAGE_ICE, Attribute.RESISTANCE_ICE)
					+ extraAttributeTableRow(target, "Poison", Attribute.DAMAGE_POISON, Attribute.RESISTANCE_POISON)
					+ extraAttributeTableRow(target, "Willpower", Attribute.DAMAGE_MANA, Attribute.RESISTANCE_MANA)
					+ extraAttributeTableRow(target, "Stamina", Attribute.DAMAGE_STAMINA, Attribute.RESISTANCE_STAMINA)

					+ extraAttributeTableRow(target, "Pure", Attribute.DAMAGE_PURE, Attribute.DAMAGE_PURE));

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (weather) {

			Main.mainController.setTooltipSize(360, 100);

			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>" + "<b style='color:" + Main.game.getCurrentWeather().getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(Main.game.getCurrentWeather().getName()) + "</b>" + "</div>"
					+ "<div class='title'>" + "<b>" + ((Main.game.getWeatherTimeRemaining() / 60) + 1) + " hour" + (((Main.game.getWeatherTimeRemaining() / 60) + 1) > 1 ? "s" : "") + " remaining" + "</b>" + "</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (protection) {

			Main.mainController.setTooltipSize(360, 100);

			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>Protection</div>"
					+ "<div class='subTitle'>"
					+ (owner.isWearingCondom()?"<span style='color:"+Colour.GENERIC_GOOD.toWebHexString()+";'>Wearing Condom</span>":"<span style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>No Condom</span>")
					+"</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (tattoo) {

			Main.mainController.setTooltipSize(360, 100);

			tooltipSB.setLength(0);
			tooltipSB.append("<div class='title'>Tattoos</div>"
					+ "<div class='subTitle'>"
					+ "TODO"
					+"</div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else if (copyInformation) {

			Main.mainController.setTooltipSize(360, 170);

			tooltipSB.setLength(0);
			tooltipSB.append(
					"<div class='subTitle'>"
					+(Main.game.getCurrentDialogueNode().getLabel() == "" || Main.game.getCurrentDialogueNode().getLabel() == null ? "-" : Main.game.getCurrentDialogueNode().getLabel())
					+ "</div>"
					+ "<div class='description'>"
					+ "Click to copy the currently displayed dialogue to your clipboard.</br></br>"
					+ "This scene was written by <b style='color:"+Colour.ANDROGYNOUS.toWebHexString()+";'>"
					+ Main.game.getCurrentDialogueNode().getAuthor()
					+ "</b></div>");

			Main.mainController.setTooltipContent(UtilText.parse(tooltipSB.toString()));

		} else { // Standard information:

			Main.mainController.setTooltipSize(360, 175);

			Main.mainController.setTooltipContent(UtilText.parse(
					"<div class='title'>"+title+"</div>"
					+ "<div class='description'>" + description + "</div>"));
		}

		(new Thread(new TooltipUpdateThread())).start();
	}
	
	private String getBodyPartDiv(String name, Race race, BodyCoveringType covering) {
		return "<div class='subTitle-half body'>"+ name + " - <span style='color:" + race.getColour().toWebHexString() + ";'>"+ Util.capitaliseSentence(race.getName()) + "</span></br>"
					+ owner.getCovering(covering).getColourDescriptor(true, true)+ " "+ owner.getCovering(covering).getName(owner)
				+ "</div>";
	}
	

	private String extraAttributeTableRow(GameCharacter owner, String type, Attribute damage, Attribute resist) {
		return "<div class='subTitle-third combatValue'>" + "<span style='color:" + damage.getColour().toWebHexString() + ";'>" + type + "</span>" + "</div>" + "<div class='subTitle-third combatValue'>"
				+ (owner.getAttributeValue(damage) > damage.getBaseValue()
										? "<span style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>"
										: (owner.getAttributeValue(damage) < damage.getBaseValue()
												? "<span style='color:" + Colour.GENERIC_BAD.toWebHexString() + ";'>"
												: ""))
				+ owner.getAttributeValue(damage)
				+ "</span>" + "</div>" + "<div class='subTitle-third combatValue'>"
				+ (resist == null ? "0.0"
						: (owner.getAttributeValue(resist) > 0 ? "<span style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>"
								: (owner.getAttributeValue(resist) < 0 ? "<span style='color:" + Colour.GENERIC_BAD.toWebHexString() + ";'>" : "")) + owner.getAttributeValue(resist) + "</span>")
				+ "</div>";
	}

	private String extraAttributeBonus(GameCharacter owner, Attribute bonus) {
		return "<div class='subTitle-half'>" + "<span style='color:"
				+ bonus.getColour().toWebHexString() + ";'>" + Util.capitaliseSentence(bonus.getName()) + "</span></br>" + (owner.getAttributeValue(bonus) > bonus.getBaseValue()
						? "<span style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>" : (owner.getAttributeValue(bonus) < bonus.getBaseValue() ? "<span style='color:" + Colour.GENERIC_BAD.toWebHexString() + ";'>" : ""))
				+ owner.getAttributeValue(bonus) + "</span>" + "</div>";
	}

	public TooltipInformationEventListener setInformation(String title, String description) {
		resetFields();
		this.title = title;
		this.description = description;

		return this;
	}

	public TooltipInformationEventListener setWeather() {
		resetFields();
		weather = true;

		return this;
	}

	public TooltipInformationEventListener setExtraAttributes(GameCharacter owner) {
		resetFields();
		extraAttributes = true;
		this.owner = owner;

		return this;
	}

	public TooltipInformationEventListener setOpponentExtraAttributes() {
		resetFields();
		opponentExtraAttributes = true;

		return this;
	}

	public TooltipInformationEventListener setStatusEffect(StatusEffect statusEffect, GameCharacter owner) {
		resetFields();
		this.statusEffect = statusEffect;
		this.owner = owner;

		return this;
	}

	public TooltipInformationEventListener setPerk(PerkInterface perk, GameCharacter owner) {
		resetFields();
		this.perk = perk;
		this.owner = owner;

		return this;
	}
	
	public TooltipInformationEventListener setFetish(Fetish fetish, GameCharacter owner) {
		resetFields();
		this.fetish = fetish;
		this.owner = owner;

		return this;
	}

	public TooltipInformationEventListener setLevelUpPerk(PerkInterface levelUpPerk, GameCharacter owner) {
		resetFields();
		this.levelUpPerk = levelUpPerk;
		this.owner = owner;

		return this;
	}

	public TooltipInformationEventListener setSpecialAttack(SpecialAttack specialAttack, GameCharacter owner) {
		resetFields();
		this.specialAttack = specialAttack;
		this.owner = owner;

		return this;
	}

	public TooltipInformationEventListener setSpell(Spell spell, int spellLevel, GameCharacter owner) {
		resetFields();
		this.spell = spell;
		this.spellLevel = spellLevel;
		this.owner = owner;

		return this;
	}

	public TooltipInformationEventListener setAttribute(Attribute attribute, GameCharacter owner) {
		resetFields();
		this.attribute = attribute;
		this.owner = owner;

		return this;
	}
	
	public TooltipInformationEventListener setProtection(GameCharacter owner) {
		resetFields();
		this.owner = owner;
		protection=true;

		return this;
	}
	
	public TooltipInformationEventListener setTattoo(GameCharacter owner) {
		resetFields();
		this.owner = owner;
		tattoo=true;

		return this;
	}
	
	public TooltipInformationEventListener setCopyInformation() {
		resetFields();
		copyInformation = true;

		return this;
	}
	

	private void resetFields() {
		extraAttributes = false;
		opponentExtraAttributes = false;
		weather = false;
		owner = null;
		statusEffect = null;
		perk = null;
		fetish = null;
		levelUpPerk = null;
		specialAttack = null;
		spell = null;
		spellLevel = 1;
		attribute = null;
		protection=false;
		tattoo=false;
		copyInformation=false;
	}
}
