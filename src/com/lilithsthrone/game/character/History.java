package com.lilithsthrone.game.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.lilithsthrone.game.character.attributes.Attribute;
import com.lilithsthrone.game.character.body.valueEnums.Capacity;
import com.lilithsthrone.game.character.gender.Gender;
import com.lilithsthrone.game.sex.OrificeType;
import com.lilithsthrone.game.sex.PenetrationType;
import com.lilithsthrone.game.sex.SexType;
import com.lilithsthrone.utils.Colour;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.Value;

/**
 * @since 0.1.0
 * @version 0.1.85
 * @author Innoxia
 */
public enum History {
	
	/*
	 * Sociable:
	Prostitute
	Stripper
	Barmaid/tender
	Massage therapist
	Waitress
	Receptionist
	Beautician
	Musician/singer
	Fitness trainer
	
Dominant:
	Mugger
	Gang leader
	Construction worker
	Mechanic
	Teacher
	Enforcer (low rank)
	Enforcer (high rank)
	
Compliant:
	Con-artist
	Librarian
	University student
	Writer
	Engineer
	Architect
	Lawyer
	Doctor
	Arcane researcher
	
Calm:
	Loafer
	Maid/Butler
	Receptionist
	Shop assistant
	Painter
	Nurse
	Chef
	Athlete
	Model
	 */
	
	// Partner histories:

	UNEMPLOYED(false, true, "unemployed", "-", null),
	
	// Sociable personality:
	
	PROSTITUTE(false, true, "prostitute", "-", null),
	STRIPPER(false, true, "stripper", "-", null),
	BAR_TENDER(false, true, "barmaid", "-", null),
	MASSAGE_THERAPIST(false, true, "massage therapist", "-", null),
	WAITRESS(false, true, "waitress", "-", null),
	BEAUTICIAN(false, true, "beautician", "-", null),
	MUSICIAN(false, true, "musician", "-", null),
	FITNESS_INSTRUCTOR(false, true, "fitness instructor", "-", null),
	
	// Commanding personality:
	
	MUGGER(false, true, "mugger", "-", null),
	CONSTRUCTION_WORKER(false, true, "construction worker", "-", null),
	MECHANIC(false, true, "mechanic", "-", null),
	TEACHER(false, true, "teacher", "-", null),
	ENFORCER(false, true, "enforcer", "-", null),
	HIGH_RANKING_ENFORCER(false, true, "enforcer chief", "-", null),
	
	// Analytical personality:

	CON_ARTIST(false, true, "con-artist", "-", null),
	LIBRARIAN(false, true, "librarian", "-", null),
	UNIVERSITY_STUDENT(false, true, "university student", "-", null),
	WRITER(false, true, "writer", "-", null),
	ENGINEER(false, true, "engineer", "-", null),
	ARCHITECT(false, true, "architect", "-", null),
	DOCTOR(false, true, "doctor", "-", null),
	ARCANE_RESEARCHER(false, true, "arcane researcher", "-", null),
	
	// Calm personality:

	MAID(false, true, "maid", "-", null),
	RECEPTIONIST(false, true, "receptionist", "-", null),
	SHOP_ASSISTANT(false, true, "shop assistant", "-", null),
	ARTIST(false, true, "artist", "-", null),
	NURSE(false, true, "nurse", "-", null),
	CHEF(false, true, "chef", "-", null),
	ATHLETE(false, true, "athlete", "-", null),
	MODEL(false, true, "model", "-", null),
	
	
	
	// Player histories:

	// Neutral:
	NEUTRAL(true, false, "Average", "You're average in every way.", Util.newHashMapOfValues()),

	// Good:
	STRONG(true, false, "Strong", "You work out at the gym almost every day. You're stronger than an average person.", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.STRENGTH, 5))),

	STUDIOUS(true, false, "Intelligent", "You spend a lot of time reading and studying. You're more intelligent than an average person.", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.INTELLIGENCE, 5))),

	HEALTHY(true, false, "Healthy", "You make sure to stick to a very healthy diet and go out running every day. As a result, you're fitter than an average person.", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.FITNESS, 5))),

	INNOCENT(true, false, "Innocent", "People don't <i>really</i> have sex before marriage, right?!"
			+ " But I suppose if they only do it once, it's ok, as you can't get pregnant from the first time!", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.CORRUPTION, -5))),
	
	// Bad:
	WEAK(true, false, "Weak", "You've got a small frame and puny muscles. You're weaker than an average person.", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.STRENGTH, -5))),

	BIRD_BRAIN(true, false, "Bird brain", "You sometimes forget what you were doing halfway throu- Ooh a penny! You're less intelligent than an average person.", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.INTELLIGENCE, -5))),

	UNFIT(true, false, "Unfit", "You've never done any exercise in your life, leaving you quite out of shape. You are less fit than an average person.", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.FITNESS, -5))),

	// Other:
	TOWN_BIKE(true, false, "Slut", "You've lost count of the amount of guys (and sometimes girls) that you've slept with. You have a lot of experience with flirting and seducing people." + " <span style='color:" + Colour.GENERIC_SEX.toWebHexString()
			+ ";'>You start the game having already lost your virginity.</span>", Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.DAMAGE_MANA, 5))) {
		@Override
		public void applyExtraEffects(GameCharacter character) {
			character.setVaginaVirgin(false);
			character.setAssVirgin(false);
			character.setFaceVirgin(false);

			if (character.isPlayer()) {
				character.setVirginityLoss(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.MOUTH_PLAYER), "your first boyfriend in the park");
				character.setSexCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.MOUTH_PLAYER), 130 + Util.random.nextInt(50));
				character.setCumCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.MOUTH_PLAYER), 60 + Util.random.nextInt(40));


				character.setVirginityLoss(new SexType(PenetrationType.TONGUE_PLAYER, OrificeType.VAGINA_PARTNER), "your first girlfriend as she lay back on her bed");
				character.setSexCount(new SexType(PenetrationType.TONGUE_PLAYER, OrificeType.VAGINA_PARTNER), 5 + Util.random.nextInt(20));
				

				character.setVirginityLoss(new SexType(PenetrationType.TONGUE_PARTNER, OrificeType.VAGINA_PLAYER), "your first girlfriend after you did the same for her");
				character.setSexCount(new SexType(PenetrationType.TONGUE_PARTNER, OrificeType.VAGINA_PLAYER), 5 + Util.random.nextInt(15));
				

				character.setVirginityLoss(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.VAGINA_PLAYER), "some guy in a club's toilet cubicle");
				character.setSexCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.VAGINA_PLAYER), 60 + Util.random.nextInt(30));
				character.setCumCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.VAGINA_PLAYER), 20 + Util.random.nextInt(20));
				

				character.setVirginityLoss(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.ANUS_PLAYER), "some guy in your first threesome");
				character.setSexCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.ANUS_PLAYER), 5 + Util.random.nextInt(10));
				character.setCumCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.ANUS_PLAYER), 2 + Util.random.nextInt(5));
			}

			character.setAssCapacity(Capacity.THREE_SLIGHTLY_LOOSE.getMedianValue());
			character.setVaginaCapacity(Capacity.FOUR_LOOSE.getMedianValue());
		}

		@Override
		public void revertExtraEffects(GameCharacter character) {
			character.setVaginaVirgin(true);
			character.setAssVirgin(true);
			character.setFaceVirgin(true);

			if (character.isPlayer()) {
				character.setVirginityLoss(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.MOUTH_PLAYER), "");
				character.setSexCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.MOUTH_PLAYER), 0);
				character.setCumCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.MOUTH_PLAYER), 0);


				character.setVirginityLoss(new SexType(PenetrationType.TONGUE_PLAYER, OrificeType.VAGINA_PARTNER), "");
				character.setSexCount(new SexType(PenetrationType.TONGUE_PLAYER, OrificeType.VAGINA_PARTNER), 0);
				

				character.setVirginityLoss(new SexType(PenetrationType.TONGUE_PARTNER, OrificeType.VAGINA_PLAYER), "");
				character.setSexCount(new SexType(PenetrationType.TONGUE_PARTNER, OrificeType.VAGINA_PLAYER), 0);
				

				character.setVirginityLoss(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.VAGINA_PLAYER), "");
				character.setSexCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.VAGINA_PLAYER), 0);
				character.setCumCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.VAGINA_PLAYER), 0);
				

				character.setVirginityLoss(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.ANUS_PLAYER), "");
				character.setSexCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.ANUS_PLAYER), 0);
				character.setCumCount(new SexType(PenetrationType.PENIS_PARTNER, OrificeType.ANUS_PLAYER), 0);
			}

			character.setAssCapacity(Capacity.ZERO_IMPENETRABLE.getMedianValue());
			character.setVaginaCapacity(Capacity.ONE_EXTREMELY_TIGHT.getMedianValue());

		}

		@Override
		public boolean isAvailable(GameCharacter player) {
			return player.getGender() == Gender.F_V_B_FEMALE;
		}
	};

	private static List<History> historiesList;
	
	public static List<History> getAvailableHistories(GameCharacter character) {
		historiesList = new ArrayList<>();

		for(History history : History.values()) {
			if(history.isAvailable(character) && (character.isPlayer()?history.isAvailableToPlayer():true) && (!character.isPlayer()?history.isAvailableToPartner():true)) {
				historiesList.add(history);
			}
		}
		
		return historiesList;
	}


	private String name, descriptionPlayer;
	private boolean availableToPlayer, availableToPartner;
	// Attributes modified by this Trait:
	private HashMap<Attribute, Integer> attributeModifiers;

	private History(boolean availableToPlayer, boolean availableToPartner, String name, String descriptionPlayer, HashMap<Attribute, Integer> attributeModifiers) {
		this.availableToPlayer = availableToPlayer;
		this.availableToPartner = availableToPartner;
		this.name = name;
		this.descriptionPlayer = descriptionPlayer;
		
		if(attributeModifiers == null) {
			this.attributeModifiers = Util.newHashMapOfValues();
		} else {
			this.attributeModifiers = attributeModifiers;
		}
	}
	
	public boolean isAvailable(GameCharacter character) {
		return true;
	}

	public void applyExtraEffects(GameCharacter character) {
	}

	public void revertExtraEffects(GameCharacter character) {
	}

	public boolean isAvailableToPlayer() {
		return availableToPlayer;
	}
	public boolean isAvailableToPartner() {
		return availableToPartner;
	}
	public String getName() {
		return name;
	}

	public String getDescriptionPlayer() {
		return descriptionPlayer;
	}

	private StringBuilder descriptionSB;

	public String getModifiersAsStringList() {
		descriptionSB = new StringBuilder();
		int i = 0;
		if (attributeModifiers != null)
			for (Entry<Attribute, Integer> e : attributeModifiers.entrySet()) {
				if (i != 0)
					descriptionSB.append("</br>");
				descriptionSB.append("<b>" + (e.getValue() > 0 ? "+" : "") + e.getValue() + "</b> <b style='color:" + Colour.GENERIC_EXCELLENT.toWebHexString() + ";'>core</b> " + "<b style='color: " + e.getKey().getColour().toWebHexString() + ";'>"
						+ Util.capitaliseSentence(e.getKey().getName()) + "</b>");
				i++;
			}
		return descriptionSB.toString();
	}

	public HashMap<Attribute, Integer> getAttributeModifiers() {
		return attributeModifiers;
	}
}