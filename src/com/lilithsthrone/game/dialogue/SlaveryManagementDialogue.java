package com.lilithsthrone.game.dialogue;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.lilithsthrone.game.character.attributes.AffectionLevel;
import com.lilithsthrone.game.character.attributes.ObedienceLevel;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.dialogue.eventLog.SlaveryEventLogEntry;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseEffectsOnly;
import com.lilithsthrone.game.dialogue.utils.InventoryInteraction;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.slavery.SlaveJob;
import com.lilithsthrone.game.slavery.SlaveJobHours;
import com.lilithsthrone.game.slavery.SlaveJobSetting;
import com.lilithsthrone.game.slavery.SlavePermission;
import com.lilithsthrone.game.slavery.SlavePermissionSetting;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.rendering.SVGImages;
import com.lilithsthrone.utils.Colour;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.world.Cell;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.GenericPlace;
import com.lilithsthrone.world.places.PlaceUpgrade;

/**
 * @since 0.1.8?
 * @version 0.1.89
 * @author Innoxia
 */
public class SlaveryManagementDialogue {
	
	private static StringBuilder miscDialogueSB = new StringBuilder();
	private static int dayNumber = 1;
	private static DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	static {
		decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
	}
	
	public static DialogueNodeOld getSlaveryOverviewDialogue() {
		dayNumber = Main.game.getDayNumber();
		return SLAVERY_OVERVIEW;
	}
	
	public static DialogueNodeOld getSlaveryManagementInspectSlaveDialogue(NPC slave) {
		Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(slave);
		return SLAVE_MANAGEMENT_INSPECT;
	}
	
	public static DialogueNodeOld getSlaveryManagementSlaveJobsDialogue(NPC slave) {
		Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(slave);
		return SLAVE_MANAGEMENT_JOBS;
	}
	
	public static DialogueNodeOld getSlaveryManagementSlavePermissionsDialogue(NPC slave) {
		Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(slave);
		return SLAVE_MANAGEMENT_PERMISSIONS;
	}
	
	public static DialogueNodeOld getSlaveryManagementDialogue(NPC slaveTrader) {
		Main.game.getDialogueFlags().setSlaveTrader(slaveTrader);
		return SLAVE_MANAGEMENT;
	}
	
	public static int getDayNumber() {
		return dayNumber;
	}

	public static void incrementDayNumber(int increment) {
		dayNumber += increment;
		dayNumber = Math.max(1,
				dayNumber);
	}
	
	private static Response getSlaveryResponse(int index) {
		if (index == 1) {
			return new Response("Slavery Overview", "View the slavery overview screen.",  SLAVERY_OVERVIEW) {
				@Override
				public void effects() {
					Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(null);
				}
			};
			
		} else if (index == 2) {
			return new Response("Slave List", "Enter the slave management screen.", SLAVE_MANAGEMENT) {
				@Override
				public DialogueNodeOld getNextDialogue() {
					return SlaveryManagementDialogue.getSlaveryManagementDialogue(Main.game.getDialogueFlags().getSlaveTrader());
				}
				@Override
				public void effects() {
					Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(null);
				}
			};
			
		} else if (index == 3) {
			return new Response("Room List", "View the room management screen.", ROOM_MANAGEMENT) {
				@Override
				public void effects() {
					Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(null);
				}
			};
			
		} else if (index == 5) {
			if(Main.game.getSlaveryUtil().getGeneratedBalance()==0) {
				return new Response("Collect: "+UtilText.formatAsMoneyUncoloured(Main.game.getSlaveryUtil().getGeneratedBalance(), "span"), "Your current balance is 0...",  null);
				
			} else if(Main.game.getSlaveryUtil().getGeneratedBalance()>0) {
				return new Response("Collect: "+UtilText.formatAsMoney(Main.game.getSlaveryUtil().getGeneratedBalance(), "span"), "Collect the money that you've earned through your slaves' activities.",  SLAVERY_OVERVIEW) {
					@Override
					public DialogueNodeOld getNextDialogue() {
						return Main.game.getCurrentDialogueNode();
					}
					@Override
					public void effects() {
						Main.game.getSlaveryUtil().payOutBalance();
					}
				};
				
			} else {
				if(Main.game.getPlayer().getMoney()<Math.abs(Main.game.getSlaveryUtil().getGeneratedBalance())) {
					return new Response("Pay: "+UtilText.formatAsMoneyUncoloured(Math.abs(Main.game.getSlaveryUtil().getGeneratedBalance()), "span"),
							"You don't have enough money to pay off the accumulated debt from the upkeep of your slaves and rooms.",  null);
				}
				
				return new Response("Pay: "+UtilText.formatAsMoney(Math.abs(Main.game.getSlaveryUtil().getGeneratedBalance()), "span", Colour.GENERIC_BAD), "Pay off the accumulated debt from the upkeep of your slaves and rooms.",  SLAVERY_OVERVIEW) {
					@Override
					public DialogueNodeOld getNextDialogue() {
						return Main.game.getCurrentDialogueNode();
					}
					@Override
					public void effects() {
						Main.game.getSlaveryUtil().payOutBalance();
					}
				};
			}
			
		} else if (index == 6) {
			if(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected() == null) {
				return new Response("Inspect", "No slave has been selected", null);
				
			}
			return new Response("Inspect", "Enter the slave management screen.", SLAVE_MANAGEMENT_INSPECT);
			
		} else if (index == 7) {
			if(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected() == null) {
				return new Response("Job", "No slave has been selected.", null);
				
			} else if(!Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected().getOwner().isPlayer()) {
				return new Response("Job", "You cannot manage the job of a slave you do not own!", null);
			}
			return new Response("Job", "Set this slave's job and work hours.", SLAVE_MANAGEMENT_JOBS);
			
		} else if (index == 8) {
			if(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected() == null) {
				return new Response("Permissions", "No slave has been selected", null);
				
			} else if(!Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected().getOwner().isPlayer()) {
				return new Response("Permissions", "You cannot manage the permissions of a slave you do not own!", null);
			}
			return new Response("Permissions", "Set this slave's permissions.", SLAVE_MANAGEMENT_PERMISSIONS);
			
		} else if (index == 9) {
			if(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected() == null) {
				return new Response("Inventory", "No slave has been selected", null);
				
			} else if(!Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected().getOwner().isPlayer()) {
				return new Response("Job", "You cannot manage the inventory of a slave you do not own!", null);
			}
			
			if(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected().getOwner().isPlayer()) {
				return new ResponseEffectsOnly("Inventory", UtilText.parse(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected(), "Manage [npc.name]'s inventory.")){
					@Override
					public void effects() {
						Main.mainController.openInventory(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected(), InventoryInteraction.FULL_MANAGEMENT);
					}
				};
			} else {
				return new Response("Inventory", UtilText.parse(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected(), "You can't manage [npc.name]'s inventory, as you don't own [npc.herHim]!"), null);
			}
			
		}else if (index == 0) {
			return new Response("Back", "Exit the room upgrades screen.", SLAVERY_OVERVIEW) {
				@Override
				public DialogueNodeOld getNextDialogue() {
					return Main.game.getActiveWorld().getCell(Main.game.getPlayer().getLocation()).getPlace().getDialogue(true);
				}
				@Override
				public void effects() {
					Main.game.getDialogueFlags().setSlaveryManagerSlaveSelected(null);
					Main.game.getDialogueFlags().setSlaveTrader(null);
				}
			};

		} else {
			return null;
		}
	}
	
	public static final DialogueNodeOld SLAVERY_OVERVIEW = new DialogueNodeOld("Slavery Overview", ".", true) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			int income = Main.game.getPlayer().getSlaveryTotalDailyIncome();
			int upkeep = Main.game.getPlayer().getSlaveryTotalDailyUpkeep();
			
			// Overview:
			UtilText.nodeContentSB.append(
					"<div class='container-full-width' style='text-align:center;'>"
						+ "<h6 style='color:"+Colour.GENERIC_EXPERIENCE.toWebHexString()+"; text-align:center;'>Totals</h6>"
						+ "<div class='container-full-width' style='text-align:center; margin-bottom:0;'>"
							+ "<div style='width:10%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ "Slaves"
							+ "</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "<b style='color:"+Colour.GENERIC_GOOD.toWebHexString()+";'>Income</b>"
							+"</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>Upkeep</b>"
							+"</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Profit</b>"
							+"</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "<b style='color:"+Colour.GENERIC_GOOD.toWebHexString()+";'>Funds</b>"
							+"</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>Payments</b>"
							+"</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Balance</b>"
							+"</div>"
						+ "</div>"
						+ "<div class='container-full-width inner' style='text-align:center;'>"
							+ "<div style='width:10%; float:left; margin:0; padding:0;'>"
								+ Main.game.getPlayer().getSlavesOwned().size()
							+ "</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ UtilText.formatAsMoney(income)+"/day"
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ UtilText.formatAsMoney(upkeep, "b", Colour.GENERIC_BAD)+"/day"
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ UtilText.formatAsMoney(income-upkeep, "b", (income-upkeep<0?Colour.GENERIC_BAD:Colour.TEXT))+"/day"
							+"</div>"
							+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ UtilText.formatAsMoney(Main.game.getSlaveryUtil().getGeneratedIncome(), "b")
							+ "</div>"
							+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ UtilText.formatAsMoney(Main.game.getSlaveryUtil().getGeneratedUpkeep(), "b", Colour.GENERIC_BAD)
							+ "</div>"
							+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ (Main.game.getSlaveryUtil().getGeneratedBalance()<0
										? UtilText.formatAsMoney(Main.game.getSlaveryUtil().getGeneratedBalance(), "b", Colour.GENERIC_BAD)
										: UtilText.formatAsMoney(Main.game.getSlaveryUtil().getGeneratedBalance(), "b"))
							+ "</div>"
						+ "</div>"
					+"</div>");
			
			// Logs:
			UtilText.nodeContentSB.append(
					"<div class='container-full-width' style='text-align:center;'>"
						+ "<h6 style='color:"+Colour.GENERIC_ARCANE.toWebHexString()+"; text-align:center;'>Activity Log</h6>"
									+ "<div id='PREVIOUS_DAY' class='normal-button' style='width:15%; margin-right:8px;'>Previous</div>"
									+ "<b>Day: "+dayNumber+(dayNumber==Main.game.getDayNumber()?" (Today)":"")+(dayNumber==Main.game.getDayNumber()-1?" (Yesterday)":"")+"</b>"
									+ "<div id='NEXT_DAY' class='normal-button' style='width:15%; margin-left:8px;'>Next</div>"

						
						+ "<div class='container-full-width' style='text-align:center; margin-bottom:0;'>"
							+ "<div style='width:10%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ "Time"
							+ "</div>"
							+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ "Slave"
							+ "</div>"
							+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
								+ "Event"
							+"</div>"
							+ "<div style='float:left; width:40%; font-weight:bold; margin:0; padding:0;'>"
								+ "Description"
							+"</div>"
							+ "<div style='float:left; width:20%; font-weight:bold; margin:0; padding:0;'>"
								+ "Effects"
							+"</div>"
						+ "</div>"
						+ "<div class='container-full-width' style='text-align:center; margin-bottom:0;'>");
			
			int count=0;
			if(Main.game.getSlaveryUtil().getSlaveryEventLogDay().get(dayNumber)!=null) {
				for(SlaveryEventLogEntry entry : Main.game.getSlaveryUtil().getSlaveryEventLogDay().get(dayNumber)) {
					if(count%2==0) {
						UtilText.nodeContentSB.append("<div class='container-full-width inner' style='background:#222222;'>");
					} else {
						UtilText.nodeContentSB.append("<div class='container-full-width inner' style='background:#292929;'>");
					}
					
					UtilText.nodeContentSB.append(
								"<div style='width:10%; float:left; margin:0; padding:0;'>"
									+ String.format("%02d", entry.getTime()) + ":00</br>"
								+ "</div>"
								+ "<div style='width:15%; float:left; margin:0; padding:0;'>"
									 + entry.getSlaveName()
								+ "</div>"
								+ "<div style='width:15%; float:left; margin:0; padding:0;'>"
									+ entry.getName()
								+ "</div>"
								+ "<div style='width:40%; float:left;  margin:0; padding:0;'>"
									+ entry.getDescription()
								+ "</div>"
								+ "<div style='width:20%; float:left;  margin:0; padding:0;'>");
					
					boolean effectsAdded = false;
					if(entry.getEffects()!=null) {
						for(String s : entry.getEffects()) {
							if(!s.isEmpty()) {
								UtilText.nodeContentSB.append(s+"</br>");
								effectsAdded = true;
							}
						}
					}
					if(!effectsAdded) {
						UtilText.nodeContentSB.append("[style.colourDisabled(-)]");
					}
					
					UtilText.nodeContentSB.append("</div>"
							+"</div>");
					count++;
				}
			}
			if(count==0) {
				UtilText.nodeContentSB.append("<div class='container-full-width inner' style='background:#222222;'>[style.colourDisabled(No events for this day...)]</div>");
			}
			
			UtilText.nodeContentSB.append("</div>"
					+ "</div>");
			
			return UtilText.nodeContentSB.toString();
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if(index==1) {
				return new Response("Slavery Overview", "You are already viewing the slavery overview screen.",  null);
			}
			
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
	};
	
	
	
	public static final DialogueNodeOld ROOM_MANAGEMENT = new DialogueNodeOld("Room Management", ".", true) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			Cell cell = Main.game.getPlayerCell();
			GenericPlace place = cell.getPlace();
			List<NPC> charactersPresent = Main.game.getCharactersTreatingCellAsHome(cell);
			float affectionChange = place.getAffectionChange();
			float obedienceChange = place.getObedienceChange();
			UtilText.nodeContentSB.append(
					"<div class='container-full-width' style='text-align:center;'>"
						+ "<h6 style='color:"+Colour.GENERIC_GOOD.toWebHexString()+"; text-align:center;'>Current Location</h6>"
						+ getRoomHeader()
						+ getRoomEntry(!place.isAbleToBeUpgraded(), true, cell, charactersPresent, affectionChange, obedienceChange)
					+"</div>");

			importantCells.clear();
			// Lilaya's home:
			UtilText.nodeContentSB.append(getWorldRooms(WorldType.LILAYAS_HOUSE_GROUND_FLOOR));
			UtilText.nodeContentSB.append(getWorldRooms(WorldType.LILAYAS_HOUSE_FIRST_FLOOR));

			// Slaver alley:
			UtilText.nodeContentSB.append(getWorldRooms(WorldType.SLAVER_ALLEY));
			
			return UtilText.nodeContentSB.toString();
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 3) {
				return new Response("Room List", "You are already viewing the room management screen.", null);
				
			}
			
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
	};
	
	private static String getRoomHeader() {
		return "<div class='container-full-width' style='margin-bottom:0;'>"
					+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
						+ "Room Name"
					+ "</div>"
					+ "<div style='width:20%; float:left; font-weight:bold; margin:0; padding:0;'>"
						+ "Occupants"
					+ "</div>"
					+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
						+ "<b>Capacity</b>"
					+"</div>"
					+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
						+ "<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>Affection</b>"
					+"</div>"
					+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
						+ "<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience</b>"
					+"</div>"
					+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
						+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Upkeep</b>"
					+"</div>"
					+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
						+ "Actions"
					+"</div>"
				+ "</div>";
	}
	
	private static String getRoomEntry(boolean disabled, boolean currentLocation, Cell cell, List<NPC> occupants, float affectionChange, float obedienceChange) {
		miscDialogueSB.setLength(0);
		
		GenericPlace place = cell.getPlace();
		
		miscDialogueSB.append(
				"<div class='container-full-width inner' style='margin-bottom:4px; margin-top:4px; "+(!occupants.isEmpty()?"background:#292929;'":"'")+"'>"
						+ "<div style='width:15%; float:left; margin:0; padding:0;'>"
							+ "<span style='color:"+place.getColour().toWebHexString()+";'>"+place.getName()+"</span></br>"
						+ "</div>"
						+ "<div style='width:20%; float:left; margin:0; padding:0;'>");
		
		int i=0;
		for(NPC occupant : occupants) {
			if(occupant.isSlave()) {
				miscDialogueSB.append("<b style='color:"+occupant.getFemininity().getColour().toWebHexString()+";'>"+occupant.getName()+"</b>"+(i+1==occupants.size()?"":"</br>"));
				i++;
			}
		}
		if(i==0) {
			miscDialogueSB.append("<b style='color:"+Colour.TEXT_GREY.toWebHexString()+";'>Empty</b>");
		}
		
		miscDialogueSB.append("</div>"
						+ "<div style='float:left; width:10%; margin:0; padding:0;'>"
							+ i+"/"+place.getCapacity()
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ "<span style='color:"+(affectionChange==0?Colour.BASE_GREY:(affectionChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(affectionChange>0?"+":"")
								+decimalFormat.format(affectionChange)+"</span>/hour"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ "<span style='color:"+(obedienceChange==0?Colour.BASE_GREY:(obedienceChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(obedienceChange>0?"+":"")
								+decimalFormat.format(obedienceChange)+"</span>/hour"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ (disabled
									?"[style.colourDisabled(N/A)]"
									:(place.getUpkeep()>0
										?UtilText.formatAsMoney(-place.getUpkeep(), "span", Colour.GENERIC_BAD)
										:(place.getUpkeep()==0
											?UtilText.formatAsMoney(-place.getUpkeep(), "span", Colour.TEXT_GREY)
											:UtilText.formatAsMoney(-place.getUpkeep(), "span", Colour.GENERIC_GOOD))))+"/day"
						+"</div>"
						+ "<div style='float:left; width:10%; margin:0 auto; padding:0; display:inline-block; text-align:center;'>"
							+ (disabled
									?"<div id='"+cell.getId()+(currentLocation?"_PRESENT":"")+"_DISABLED' class='square-button solo disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveInspectDisabled()+"</div></div>"
									:"<div id='"+cell.getId()+(currentLocation?"_PRESENT":"")+"' class='square-button solo'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveInspect()+"</div></div>")
						+"</div>"
					+ "</div>");
		
		return miscDialogueSB.toString();
	}
	
	public static List<Cell> importantCells = new ArrayList<>();
	
	static {
		if(Main.game.getWorlds().get(WorldType.LILAYAS_HOUSE_GROUND_FLOOR) != null) { 
			getWorldRooms(WorldType.LILAYAS_HOUSE_GROUND_FLOOR);
			getWorldRooms(WorldType.LILAYAS_HOUSE_FIRST_FLOOR);
			getWorldRooms(WorldType.SLAVER_ALLEY);
		}
	}
	
	private static String getWorldRooms(WorldType worldType) {
		StringBuilder worldRoomSB = new StringBuilder();
		
		worldRoomSB.append(
				"<div class='container-full-width' style='text-align:center;'>"
					+ "<h6 style='color:"+worldType.getColour().toWebHexString()+"; text-align:center;'>"+worldType.getName()+"</h6>"
					+ getRoomHeader());
		
		Cell[][] cellGrid = Main.game.getWorlds().get(worldType).getCellGrid();
		List<Cell> sortingCells = new ArrayList<>();
		for(int i = 0; i< cellGrid.length; i++) {
			for(int j = 0; j < cellGrid[0].length; j++) {
				if(!cellGrid[i][j].getPlace().getPlaceUpgrades().isEmpty()) {
					importantCells.add(cellGrid[i][j]);
					sortingCells.add(cellGrid[i][j]);
				}
			}
		}
		
		sortingCells.sort(Comparator.comparing(Cell::getPlaceName));

		for(Cell c : sortingCells) {
			GenericPlace place = c.getPlace();
			worldRoomSB.append(getRoomEntry(!place.isAbleToBeUpgraded(), false, c, Main.game.getCharactersTreatingCellAsHome(c), place.getAffectionChange(), place.getObedienceChange()));
		}
		
		worldRoomSB.append("</div>");
		
		return worldRoomSB.toString();
	}
	
	
	public static Cell cellToInspect;
	public static final DialogueNodeOld ROOM_UPGRADES = new DialogueNodeOld("Room Management", ".", true) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getLabel() {
			return cellToInspect.getPlace().getName()+" Management";
		}

		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append(
					"<div class='container-full-width' style='text-align:center;'>"
							+ "<h6 style='color:"+Colour.GENERIC_EXCELLENT.toWebHexString()+"; text-align:center;'>Overview (Total Values for this Room)</h6>"
							+"<div class='container-full-width' style='margin-bottom:0;'>"
								+ "<div style='width:20%; float:left; font-weight:bold; margin:0; padding:0;'>"
									+ "(Re)Name"
								+ "</div>"
								+ "<div style='width:20%; float:left; font-weight:bold; margin:0; padding:0;'>"
									+ "Occupants"
								+ "</div>"
								+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b>Capacity</b>"
								+"</div>"
								+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>Affection</b>"
								+"</div>"
								+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience</b>"
								+"</div>"
								+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Upkeep</b>"
								+"</div>"
							+ "</div>");
			
			
			GenericPlace place = cellToInspect.getPlace();
			float affectionChange = place.getAffectionChange();
			float obedienceChange = place.getObedienceChange();
			
			UtilText.nodeContentSB.append(
					"<div class='container-full-width inner' style='margin-bottom:0;'>"
							+ "<div style='width:20%; float:left; font-weight:bold; margin:0; padding:0;'>"
								+ "<form style='float:left; width:85%; margin:0; padding:0;'><input type='text' id='nameInput' value='"+ cellToInspect.getPlace().getName()+ "' style='width:100%; margin:0; padding:0;'></form>"
								+ "<div class='SM-button' id='rename_room_button' style='float:left; width:15%; height:28px; line-height:28px; margin:0; padding:0;'>"
									+ "&#10003;"
								+ "</div>"
							+ "</div>"
							+ "<div style='width:20%; float:left; margin:0; padding:0;'>");
			
			int i=0;
			List<NPC> occupants = Main.game.getCharactersTreatingCellAsHome(cellToInspect);
			for(NPC occupant : occupants) {
				if(occupant.isSlave()) {
					UtilText.nodeContentSB.append("<b style='color:"+occupant.getFemininity().getColour().toWebHexString()+";'>"+occupant.getName()+"</b>"+(i+1==occupants.size()?"":"</br>"));
					i++;
				}
			}
			if(i==0) {
				UtilText.nodeContentSB.append("<b style='color:"+Colour.TEXT_GREY.toWebHexString()+";'>Empty</b>");
			}
			
			
			UtilText.nodeContentSB.append(
					"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ i+"/"+place.getCapacity()
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ "<span style='color:"+(affectionChange==0?Colour.BASE_GREY:(affectionChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(affectionChange>0?"+":"")
									+decimalFormat.format(affectionChange)+"</span>/day"
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ "<span style='color:"+(obedienceChange==0?Colour.BASE_GREY:(obedienceChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(obedienceChange>0?"+":"")
									+decimalFormat.format(obedienceChange)+"</span>/day"
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ (place.getUpkeep()>0
											?UtilText.formatAsMoney(-place.getUpkeep(), "span", Colour.GENERIC_BAD)
											:UtilText.formatAsMoney(-place.getUpkeep(), "span", Colour.GENERIC_GOOD))+"/day"
							+"</div>"
						+ "</div>"
						+ "</div>");
			
			
			// Normal upgrades:
			UtilText.nodeContentSB.append("<div class='container-full-width' style='text-align:center;'>"
											+ "<h6 style='color:"+Colour.GENERIC_GOOD.toWebHexString()+"; text-align:center;'>Modifications</h6>"
											+ getRoomUpgradeHeader());
			
			List<PlaceUpgrade> coreUpgrades = new ArrayList<>();
			for(PlaceUpgrade upgrade : place.getPlaceType().getAvailablePlaceUpgrades()) {
				if(upgrade.isCoreRoomUpgrade()) {
					coreUpgrades.add(upgrade);
				} else {
					UtilText.nodeContentSB.append(getUpgradeEntry(cellToInspect, upgrade));
					i++;
				}
			}
			if(i==0) {
				UtilText.nodeContentSB.append("<div class='container-full-width inner' style='background:#292929;'><b style='color:"+Colour.TEXT_GREY.toWebHexString()+";'>No Modifications Available</b></div>");
			}
			
			UtilText.nodeContentSB.append("</div>");
			
			// Core upgrades:
			UtilText.nodeContentSB.append("<div class='container-full-width' style='text-align:center;'>"
					+ "<h6 style='color:"+Colour.GENERIC_ARCANE.toWebHexString()+"; text-align:center;'>Core Modifications</h6>"
					+ getRoomUpgradeHeader());

			
			for (PlaceUpgrade upgrade : place.getPlaceUpgrades()) {
				if(upgrade.isCoreRoomUpgrade()) {
					UtilText.nodeContentSB.append(getUpgradeEntry(cellToInspect, upgrade));
				}
			}
			
			i = 0;
			for (PlaceUpgrade upgrade : coreUpgrades) {
				UtilText.nodeContentSB.append(getUpgradeEntry(cellToInspect, upgrade));
				i++;
			}
			if(i==0) {
				UtilText.nodeContentSB.append("<div class='container-full-width inner' style='background:#292929;'><b style='color:"+Colour.TEXT_GREY.toWebHexString()+";'>No Core Modifications Available</b></div>");
			}
			
			UtilText.nodeContentSB.append("</div>"
					+ "<p id='hiddenFieldName' style='display:none;'></p>");
			
			return UtilText.nodeContentSB.toString();
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
	};
	
	
	private static String getRoomUpgradeHeader() {
		return "<div class='container-full-width' style='margin-bottom:0;'>"
					+ "<div style='width:30%; float:left; font-weight:bold; margin:0; padding:0;'>"
						+ "Upgrade"
					+ "</div>"
					+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
						+ "Capacity"
					+"</div>"
					+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
						+ "<span style='color:"+Colour.AFFECTION.toWebHexString()+";'>Affection</span>"
					+"</div>"
					+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
						+ "<span style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience</span>"
					+"</div>"
					+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
						+ "<span style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Upkeep</span>"
					+"</div>"
					+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
						+ "<span style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Cost</span>"
					+"</div>"
					+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
						+ "Actions"
					+"</div>"
				+ "</div>";
	}
	
	private static String getUpgradeEntry(Cell cell, PlaceUpgrade upgrade) {
		miscDialogueSB.setLength(0);
		GenericPlace place = cell.getPlace();
		float affectionChange = upgrade.getAffectionGain();
		float obedienceChange = upgrade.getObedienceGain();
		boolean owned = place.getPlaceUpgrades().contains(upgrade);
		boolean availableForPurchase = upgrade.isPrerequisitesMet(place) && upgrade.isAvailable(cell) && (owned?Main.game.getPlayer().getMoney()>=upgrade.getRemovalCost():Main.game.getPlayer().getMoney()>=upgrade.getInstallCost());
		boolean canBuy = availableForPurchase;
		
		miscDialogueSB.append(
				"<div class='container-full-width inner' style='margin-bottom:4px; margin-top:4px;"+(owned?"background:#292929;'":"'")+"'>"
						+ "<div style='width:30%; float:left; margin:0; padding:0;'>"
							+ (owned
									?"<b style='color:"+Colour.GENERIC_GOOD.toWebHexString()+";'>"+Util.capitaliseSentence(upgrade.getName())+"</b>"
									:(!availableForPurchase
											?"<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+Util.capitaliseSentence(upgrade.getName())+"</b>"
											:"<b>"+Util.capitaliseSentence(upgrade.getName())+"</b>"))
						+ "</div>"
						+ "<div style='width:10%; float:left; margin:0; padding:0;'>"
							+ (upgrade.getCapacity()>0
									?"<b style='color:"+Colour.GENERIC_EXCELLENT.toWebHexString()+";'>+"+upgrade.getCapacity()+"</b>"
									:(upgrade.getCapacity()<0
											?"<b style='color:"+Colour.GENERIC_TERRIBLE.toWebHexString()+";'>"+upgrade.getCapacity()+"</b>"
											:"[style.colourDisabled(0)]"))
						+ "</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ (affectionChange>0
									?"<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>+"+decimalFormat.format(affectionChange)+ "</b>/day"
									:(affectionChange<0
											?"<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+decimalFormat.format(affectionChange)+ "</b>/day"
											:"[style.colourDisabled(0)]/day"))
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ (obedienceChange>0
									?"<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>+"+decimalFormat.format(obedienceChange)+ "</b>/day"
									:(obedienceChange<0
											?"<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+decimalFormat.format(obedienceChange)+ "</b>/day"
											:"[style.colourDisabled(0)]/day"))
						+"</div>"
						+ "<div style='float:left; width:10%; margin:0; padding:0;'>"
							+ (upgrade.getUpkeep()>0
									?UtilText.formatAsMoney(upgrade.getUpkeep(), "b", Colour.GENERIC_BAD)
									:UtilText.formatAsMoney(upgrade.getUpkeep(), "b", Colour.GENERIC_GOOD))+"/day"
						+"</div>"
						+ "<div style='float:left; width:10%; margin:0; padding:0;'>"
							+ (owned
									?(upgrade.getRemovalCost()<0
											?UtilText.formatAsMoney(upgrade.getRemovalCost(), "b", Colour.GENERIC_GOOD)
											:(upgrade.getRemovalCost() < Main.game.getPlayer().getMoney()
													?UtilText.formatAsMoney(upgrade.getRemovalCost(), "b")
													:UtilText.formatAsMoney(upgrade.getRemovalCost(), "b", Colour.GENERIC_BAD)))
									:(upgrade.getInstallCost()<0
											?UtilText.formatAsMoney(upgrade.getInstallCost(), "b", Colour.GENERIC_GOOD)
											:(upgrade.getInstallCost() < Main.game.getPlayer().getMoney()
													?UtilText.formatAsMoney(upgrade.getInstallCost(), "b")
													:UtilText.formatAsMoney(upgrade.getInstallCost(), "b", Colour.GENERIC_BAD))))
						+"</div>"
						+ "<div style='float:left; width:10%; margin:0 auto; padding:0; display:inline-block; text-align:center;'>");
		
		if(owned) {
			if(Main.game.getPlayer().getMoney()<upgrade.getRemovalCost() || upgrade.isCoreRoomUpgrade()) {
				miscDialogueSB.append("<div id='"+upgrade+"_SELL_DISABLED' class='square-button solo disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionSellDisabled()+"</div></div>");
			} else {
				miscDialogueSB.append("<div id='"+upgrade+"_SELL' class='square-button solo'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionSell()+"</div></div>");
			}
			
		} else {
			if(Main.game.getPlayer().getMoney()<upgrade.getInstallCost() || Main.game.getSlaveryUtil().getGeneratedBalance()<0) {
				canBuy = false;
			}
			if(canBuy) {
				if(!upgrade.getPrerequisites().isEmpty()) {
					for(PlaceUpgrade prereq : upgrade.getPrerequisites()) {
						if(!place.getPlaceUpgrades().contains(prereq)) {
							canBuy = false;
							break;
						}
					}
				}
			}
			
			if(canBuy) {
				miscDialogueSB.append("<div id='"+upgrade+"_BUY' class='square-button solo'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionBuy()+"</div></div>");
			} else {
				miscDialogueSB.append("<div id='"+upgrade+"_BUY_DISABLED' class='square-button solo disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionBuyDisabled()+"</div></div>");
			}
		}
		
		miscDialogueSB.append(
				"</div>"
					+ "<p>"
						+ "<i>"
							+(!owned
								?"[style.colourDisabled("+upgrade.getDescriptionForPurchase()+")]"
								:upgrade.getDescriptionAfterPurchase())
						+"</i>"
					+ "</p>"
					+ (upgrade.isCoreRoomUpgrade() && !owned
							?"<p>This is a [style.boldArcane(core modification)], and will [style.boldBad(remove all other modifications in this room when purchased)].</p>"
							:""));
		
		if(!canBuy) {
			miscDialogueSB.append("<p>"
				+ "<i>"
				+ "[style.colourBad("+ getPurchaseAvailabilityTooltipText(SlaveryManagementDialogue.cellToInspect.getPlace(), upgrade)+")]"
				+"</i>"
			+ "</p>");
		}
		
		miscDialogueSB.append("</div>");
		
		return miscDialogueSB.toString();
	}
	
	private static StringBuilder purchaseAvailability = new StringBuilder();
	public static String getPurchaseAvailabilityTooltipText(GenericPlace place, PlaceUpgrade upgrade) {
		boolean owned = place.getPlaceUpgrades().contains(upgrade);
		
		purchaseAvailability.setLength(0);
		
		if(owned) {
			if(Main.game.getPlayer().getMoney()<upgrade.getRemovalCost()) {
				purchaseAvailability.append("<span style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>You cannot afford to remove this modification.</span>");
			}
			
		} else {
			
			if(Main.game.getSlaveryUtil().getGeneratedBalance()<0) {
				purchaseAvailability.append("<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>You cannot purchase any modifications while your slavery balance is negative.</b>");
			}
			
			if(Main.game.getPlayer().getMoney()<upgrade.getInstallCost()) {
				purchaseAvailability.append("<span style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>You cannot afford this modification.</span>");
			}
			
			if(!upgrade.getPrerequisites().isEmpty()) {
				purchaseAvailability.append("You need to purchase the following first:");
				for(PlaceUpgrade prereq : upgrade.getPrerequisites()) {
					if(place.getPlaceUpgrades().contains(prereq)) {
						purchaseAvailability.append("</br><span style='color:"+Colour.GENERIC_GOOD.toWebHexString()+";'>"+prereq.getName()+"</span>");
					} else {
						purchaseAvailability.append("</br><span style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+prereq.getName()+"</span>");
					}
				}
			}
		}
		
		String availabilityDescription = upgrade.getAvailabilityDescription(Main.game.getPlayerCell());
		if(availabilityDescription!=null && availabilityDescription.length()>0) {
			purchaseAvailability.append("</br><span style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+availabilityDescription+"</span>");
		}
		
		return purchaseAvailability.toString();
	}
	
	
	private static final DialogueNodeOld SLAVE_MANAGEMENT = new DialogueNodeOld("Slave Management", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getContent() {
			UtilText.nodeContentSB.setLength(0);
			
			if(Main.game.getDialogueFlags().getSlaveTrader()!=null) {
				// Append for sale first:
				UtilText.nodeContentSB.append("<div class='container-full-width' style='text-align:center;'>"
						+ "<h6 style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+"; text-align:center;'>Slaves For Sale</h6>"
						
						+ getSlaveryHeader());
				int i=0;
				List<NPC> npcsPresent = new ArrayList<>(Main.game.getCharactersTreatingCellAsHome(Main.game.getPlayerCell()));
				for(NPC slave : npcsPresent) {
					if(slave.isSlave() && !slave.getOwner().isPlayer()) {
						AffectionLevel affection = AffectionLevel.getAffectionLevelFromValue(slave.getAffection(Main.game.getPlayer()));
						ObedienceLevel obedience = ObedienceLevel.getObedienceLevelFromValue(slave.getObedienceValue());
						float affectionChange = slave.getDailyAffectionChange();
						float obedienceChange = slave.getDailyObedienceChange();
						GenericPlace place = Main.game.getPlayerCell().getPlace();

						UtilText.nodeContentSB.append(getSlaveryEntry(false, place, slave, affection, affectionChange, obedience, obedienceChange, i%2==0));
						i++;
					}
				}
				if(i==0) {
					UtilText.nodeContentSB.append("<div class='container-full-width inner'><h4 style='color:"+Colour.TEXT_GREY.toWebHexString()+";'>No slaves for sale!</h4></div>");
				}
				UtilText.nodeContentSB.append("</div>");
			}
			
			
			// Your slaves:
			UtilText.nodeContentSB.append("<div class='container-full-width' style='text-align:center;'>"
					+ "<h6 style='color:"+Colour.GENERIC_GOOD.toWebHexString()+"; text-align:center;'>Slaves Owned</h6>"
					
					+ getSlaveryHeader());
			
			if(Main.game.getPlayer().getSlavesOwned().isEmpty()) {
				UtilText.nodeContentSB.append(
						"<div class='container-full-width' style='text-align:center;'>"
								+"<h5 style='color:"+Colour.BASE_GREY.toWebHexString()+";'>You do not own any slaves...</h5>"
						+ "</div>");
				
			} else {
				int i = 0;
				for(String id : Main.game.getPlayer().getSlavesOwned()) {
					NPC slave = (NPC) Main.game.getNPCById(id);
					AffectionLevel affection = AffectionLevel.getAffectionLevelFromValue(slave.getAffection(Main.game.getPlayer()));
					ObedienceLevel obedience = ObedienceLevel.getObedienceLevelFromValue(slave.getObedienceValue());
					float affectionChange = slave.getDailyAffectionChange();
					float obedienceChange = slave.getDailyObedienceChange();
					GenericPlace place = Main.game.getPlayerCell().getPlace();
					
					UtilText.nodeContentSB.append(getSlaveryEntry(true, place, slave, affection, affectionChange, obedience, obedienceChange, i%2==0));
					i++;
				}
			}
			
			UtilText.nodeContentSB.append(
					"</div>");
			
			return UtilText.nodeContentSB.toString();
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 2) {
				return new Response("Slave List", "You are already viewing the slave management screen.", null);
				
			}
			
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
	};
	
	private static String getSlaveryHeader() {
		return "<div class='container-full-width' style='margin-bottom:0;'>"
					+ "<div style='width:20%; float:left; font-weight:bold; margin:0; padding:0;'>"
					+ "Slave"
				+ "</div>"
				+ "<div style='width:20%; float:left; font-weight:bold; margin:0; padding:0;'>"
					+ "Location"
				+ "</div>"
				+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
					+ "<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>Affection</b>"
				+"</div>"
				+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
					+ "<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience</b>"
				+"</div>"
				+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
					+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Value</b>"
				+"</div>"
				+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
					+ "Actions"
				+"</div>"
			+ "</div>";
	}
	
	private static String getSlaveryEntry(boolean slaveOwned, GenericPlace place, NPC slave, AffectionLevel affection, float affectionChange, ObedienceLevel obedience, float obedienceChange, boolean alternateBackground) {
		miscDialogueSB.setLength(0);
		
		miscDialogueSB.append(
				"<div class='container-full-width inner' style='margin-bottom:0;"+(alternateBackground?"background:#292929;'":"'")+"'>"
						+ "<div style='width:20%; float:left; margin:0; padding:0;'>"
							+ "<b style='color:"+slave.getFemininity().getColour().toWebHexString()+";'>"+slave.getName()+"</b></br>"
							+ "<span style='color:"+slave.getRace().getColour().toWebHexString()+";'>"+Util.capitaliseSentence((slave.isFeminine()?slave.getRace().getSingularFemaleName():slave.getRace().getSingularMaleName()))+"</span></br>"
							+ "<span style='color:"+slave.getFemininity().getColour().toWebHexString()+";'>"+Util.capitaliseSentence(slave.getGender().getName())+"</span>"
						+ "</div>"
						+ "<div style='width:20%; float:left; margin:0; padding:0;'>"
							+ "<b style='color:"+slave.getLocationPlace().getColour().toWebHexString()+";'>"+slave.getLocationPlace().getName()+"</b>"
							+",</br>"
							+ "<span style='color:"+slave.getWorldLocation().getColour().toWebHexString()+";'>"+slave.getWorldLocation().getName()+"</span>"
						+ "</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ "<b style='color:"+affection.getColour().toWebHexString()+";'>"+slave.getAffection(Main.game.getPlayer())+ "</b>"
							+ "</br><span style='color:"+(affectionChange==0?Colour.BASE_GREY:(affectionChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(affectionChange>0?"+":"")
								+decimalFormat.format(affectionChange)+"</span>/day"
							+ "</br>"
							+ "<span style='color:"+affection.getColour().toWebHexString()+";'>"+Util.capitaliseSentence(affection.getName())+"</span>"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ "<b style='color:"+obedience.getColour().toWebHexString()+";'>"+slave.getObedienceValue()+ "</b>"
							+ "</br><span style='color:"+(obedienceChange==0?Colour.BASE_GREY:(obedienceChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(obedienceChange>0?"+":"")
								+decimalFormat.format(obedienceChange)+"</span>/day"
							+ "</br>"
							+ "<span style='color:"+obedience.getColour().toWebHexString()+";'>"+Util.capitaliseSentence(obedience.getName())+"</span>"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ (Main.game.getDialogueFlags().getSlaveTrader()!=null
								?(slaveOwned
										?UtilText.formatAsMoney((int) (slave.getValueAsSlave()*Main.game.getDialogueFlags().getSlaveTrader().getBuyModifier()), "b", Colour.GENERIC_ARCANE)
										:UtilText.formatAsMoney((int) (slave.getValueAsSlave()*Main.game.getDialogueFlags().getSlaveTrader().getSellModifier()), "b", Colour.GENERIC_ARCANE))
								:UtilText.formatAsMoney(slave.getValueAsSlave()))+"</br>"
							+ "<b>"+Util.capitaliseSentence(slave.getSlaveJob().getName(slave))+"</b></br>"
							+ UtilText.formatAsMoney(slave.getSlaveJob().getFinalDailyIncomeAfterModifiers(slave))+"/day"
						+"</div>");
		
		if(slaveOwned) {
			miscDialogueSB.append("<div style='float:left; width:15%; margin:0 auto; padding:0; display:inline-block; text-align:center;'>"
					+ "<div id='"+slave.getId()+"' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveInspect()+"</div></div>"

					+ "<div id='"+slave.getId()+"_JOB' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveJob()+"</div></div>"

					+ "<div id='"+slave.getId()+"_PERMISSIONS' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlavePermissions()+"</div></div>"
					
					+"<div id='"+slave.getId()+"_INVENTORY' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getInventoryIcon()+"</div></div>"
						
					+ "<div "+((place.getCapacity()<=Main.game.getCharactersTreatingCellAsHome(Main.game.getPlayerCell()).size())
								|| (slave.getLocation().equals(Main.game.getPlayer().getLocation()) && slave.getWorldLocation().equals(Main.game.getPlayer().getWorldLocation()))
										?" id='"+slave.getId()+"_TRANSFER_DISABLED' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveTransferDisabled()+"</div></div>"
										:" id='"+slave.getId()+"_TRANSFER' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveTransfer()+"</div></div>"));
			
			if(Main.game.getDialogueFlags().getSlaveTrader()==null) {
				miscDialogueSB.append("<div id='"+slave.getId()+"_SELL_DISABLED' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionSellDisabled()+"</div></div>");
			} else {
				miscDialogueSB.append("<div id='"+slave.getId()+"_SELL' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionSell()+"</div></div>");
			}
			
		} else { // Slave trader's slave:
			miscDialogueSB.append("<div style='float:left; width:15%; margin:0 auto; padding:0; display:inline-block; text-align:center;'>"
					+ "<div id='"+slave.getId()+"_TRADER' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveInspect()+"</div></div>"

					+ "<div id='"+slave.getId()+"_TRADER_JOB' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveJobDisabled()+"</div></div>"

					+ "<div id='"+slave.getId()+"_TRADER_PERMISSIONS' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlavePermissionsDisabled()+"</div></div>"
					
					+"<div id='"+slave.getId()+"_TRADER_INVENTORY' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getInventoryIconDisabled()+"</div></div>"
						
					+ "<div id='"+slave.getId()+"_TRADER_TRANSFER' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getSlaveTransferDisabled()+"</div></div>");
			
			if(Main.game.getPlayer().getMoney() < ((int) (slave.getValueAsSlave()*Main.game.getDialogueFlags().getSlaveTrader().getSellModifier()))) {
				miscDialogueSB.append("<div id='"+slave.getId()+"_BUY_DISABLED' class='square-button big disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionBuyDisabled()+"</div></div>");
			} else {
				miscDialogueSB.append("<div id='"+slave.getId()+"_BUY' class='square-button big'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getTransactionBuy()+"</div></div>");
			}
		}
		
		miscDialogueSB.append("</div></div>");
		
		return miscDialogueSB.toString();
	}
	
	private static StringBuilder headerSB = new StringBuilder();
	private static String getSlaveInformationHeader(NPC character) {
		headerSB.setLength(0);
		AffectionLevel affection = AffectionLevel.getAffectionLevelFromValue(character.getAffection(Main.game.getPlayer()));
		ObedienceLevel obedience = ObedienceLevel.getObedienceLevelFromValue(character.getObedienceValue());
		float affectionChange = character.getDailyAffectionChange();
		float obedienceChange = character.getDailyObedienceChange();
		
		headerSB.append(
				"<div class='container-full-width' style='text-align:center;'>"
				
					// Core naming information:
					+"<div class='container-full-width' style='margin-bottom:0;'>"
						+ "<div style='width:50%; float:left; font-weight:bold; margin:0; padding:0;'>"
							+ "Slave's Name"
						+ "</div>"
						+ "<div style='width:50%; float:left; font-weight:bold; margin:0; padding:0;'>"
							+ UtilText.parse(character, "What [npc.she] calls you")
						+ "</div>"
					+ "</div>"
					+"<div class='container-full-width inner' style='padding:8px;'>"
						+ "<div style='width:49%; float:left; font-weight:bold; margin:0 1% 0 0; padding:0;'>"
							+ "<form style='float:left; width:90%; margin:0; padding:0;'><input type='text' id='slaveNameInput' value='"+ character.getName()+ "' style='width:100%; margin:0; padding:0;'></form>"
							+ "<div class='SM-button' id='"+character.getId()+"_RENAME' style='float:left; width:9%; height:28px; line-height:28px; margin:0 0 0 1%; padding:0;'>"
								+ "&#10003;"
							+ "</div>"
						+ "</div>"
						+ "<div style='width:49%; float:left; font-weight:bold; margin:0 0 0 1%; padding:0;'>"
							+ "<form style='float:left; width:50%; margin:0; padding:0;'><input type='text' id='slaveToPlayerNameInput' value='"+ character.getPlayerPetName()+ "' style='width:100%; margin:0; padding:0;'></form>"
							+ "<div class='SM-button' id='"+character.getId()+"_CALLS_PLAYER' style='float:left; width:9%; height:28px; line-height:28px; margin:0 0 0 1%; padding:0;'>"
								+ "&#10003;"
							+ "</div>"
							+ " <div class='SM-button' id='GLOBAL_CALLS_PLAYER' style='float:left; width:39%; height:28px; line-height:28px; margin:0 0 0 1%; padding:0;'>"
								+ "All Slaves"
						+ "</div>"
					+ "</div>"
						
					// Extra core information:
					+"<div class='container-full-width' style='margin-bottom:0;'>"
						+ "<div style='width:25%; float:left; font-weight:bold; margin:0; padding:0;'>"
							+ "Location"
						+ "</div>"
						+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
							+ "<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>Affection</b>"
						+"</div>"
						+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
							+ "<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience</b>"
						+"</div>"
						+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
							+ "<b>Job</b>"
						+"</div>"
						+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
							+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Income</b>"
						+"</div>"
						+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
							+ "Value"
						+ "</div>"
					+ "</div>"
					+"<div class='container-full-width inner'>"
						+"<div style='width:25%; float:left; margin:0; padding:0;'>"
							+ "<b style='color:"+character.getLocationPlace().getColour().toWebHexString()+";'>"+character.getLocationPlace().getName()+"</b>"
							+",</br>"
							+ "<span style='color:"+character.getWorldLocation().getColour().toWebHexString()+";'>"+character.getWorldLocation().getName()+"</span>"
						+ "</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ "<b style='color:"+affection.getColour().toWebHexString()+";'>"+character.getAffection(Main.game.getPlayer())+ "</b>"
							+ "</br><span style='color:"+(affectionChange==0?Colour.BASE_GREY:(affectionChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(affectionChange>0?"+":"")
								+decimalFormat.format(affectionChange)+"</span>/day"
							+ "</br>"
							+ "<span style='color:"+affection.getColour().toWebHexString()+";'>"+Util.capitaliseSentence(affection.getName())+"</span>"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ "<b style='color:"+obedience.getColour().toWebHexString()+";'>"+character.getObedienceValue()+ "</b>"
							+ "</br><span style='color:"+(obedienceChange==0?Colour.BASE_GREY:(obedienceChange>0?Colour.GENERIC_GOOD:Colour.GENERIC_BAD)).toWebHexString()+";'>"+(obedienceChange>0?"+":"")
								+decimalFormat.format(obedienceChange)+"</span>/day"
							+ "</br>"
							+ "<span style='color:"+obedience.getColour().toWebHexString()+";'>"+Util.capitaliseSentence(obedience.getName())+"</span>"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ Util.capitaliseSentence(character.getSlaveJob().getName(character)));
						
		for(SlaveJobSetting setting : character.getSlaveJobSettings()) {
			headerSB.append("</br><span style='color:"+setting.getColour().toWebHexString()+";'>"+setting.getTag()+"</span>");
		}
		
		headerSB.append("</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ UtilText.formatAsMoney(character.getSlaveJob().getFinalDailyIncomeAfterModifiers(character))+"/day"
						+"</div>"
						+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
							+ UtilText.formatAsMoney(character.getValueAsSlave())
						+"</div>"
					+ "</div>"

					+"<div class='container-full-width inner'>"
						+ "<b style='color:"+Colour.GENERIC_ARCANE.toWebHexString()+";'>Permissions:</b></br>");
		
		int permissionCount=0;
		for(SlavePermission permission : SlavePermission.values()) {
			for(SlavePermissionSetting setting : permission.getSettings()) {
				if(character.getSlavePermissionSettings().get(permission).contains(setting)) {
					headerSB.append((permissionCount==0?"":", ")+"<span style='color:"+permission.getColour().toWebHexString()+";'>"+setting.getName()+"</span>");
					permissionCount++;
				}
			}
		}
		
		headerSB.append(".</div>"
				+ "</div>"
				+ "</div>");
		
		return headerSB.toString();
	}
	
	
	/**
	 * <b>Use getSlaveryManagementDetailedDialogue(NPC slave) to initialise this!!!</b>
	 */
	public static final DialogueNodeOld SLAVE_MANAGEMENT_INSPECT = new DialogueNodeOld("Slave Management", ".", true) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getLabel() {
			return UtilText.parse(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected(), "[npc.Name] - Slave Management");
		}
		
		@Override
		public String getContent() {
			NPC character = Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected();
			
			UtilText.nodeContentSB.setLength(0);
			
			if(character.getOwner().isPlayer()) {
				UtilText.nodeContentSB.append(getSlaveInformationHeader(character));
			}
			
			UtilText.nodeContentSB.append(
					"<div class='container-full-width'>"
							+ "<h6 style='color:"+Colour.GENERIC_EXCELLENT.toWebHexString()+"; text-align:center;'>Inspection</h6>"
							+ "<div class='container-full-width inner'>"
								+NPC.getCharacterInformationScreen(character)
							+"</div>"
					+"</div>"
					+ "<p id='hiddenFieldName' style='display:none;'></p>");
			
			return UtilText.parse(character, UtilText.nodeContentSB.toString());
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 6) {
				return new Response("Inspect", "You are already inspecting this slave.", null);
				
			}
			
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
		
		@Override
		public boolean reloadOnRestore() {
			return true;
		}
	};
	
	public static final DialogueNodeOld SLAVE_MANAGEMENT_JOBS = new DialogueNodeOld("Slave Management", ".", true) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getLabel() {
			return UtilText.parse(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected(), "[npc.Name] - Jobs");
		}
		
		@Override
		public String getContent() {
			NPC character = Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected();
			ObedienceLevel obedience = ObedienceLevel.getObedienceLevelFromValue(character.getObedienceValue());
			float affectionChange = character.getDailyAffectionChange();
			float obedienceChange = character.getDailyObedienceChange();
			
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append(getSlaveInformationHeader(character));
			
			// Job hours
			UtilText.nodeContentSB.append("<div class='container-full-width' style='text-align:center;'>"
					+ "<h6 style='text-align:center;'><span style='color:"+Colour.BASE_YELLOW.toWebHexString()+";'>Work Hours</span></h6>"
					+ "<div class='container-full-width inner'>");
			for(int i=0 ; i< 24; i++) {
				UtilText.nodeContentSB.append("<div class='normal-button hour "
						+(character.getWorkHours()[i]?" selected":"")
									+"' id='"+i+"_WORK'>"+String.format("%02d", i)+":00</div>");
			}
			
			UtilText.nodeContentSB.append("<div style='width:100%;margin-top:8px;'><b>Presets</b></div>"
					+ "<div class='container-full-width inner' style='text-align:center;'>");
			for(SlaveJobHours preset : SlaveJobHours.values()) {
				UtilText.nodeContentSB.append("<div class='normal-button' id='"+preset+"_TIME' style='width:16%; margin:2px;'>"+preset.getName()+"</div>");
			}
			UtilText.nodeContentSB.append("</div>"
					+ "</div>"
					+ "</div>");
			
			
			// Jobs:
			// TODO description box explaining that setting just influence random events
			UtilText.nodeContentSB.append(
					"<div class='container-full-width' style='text-align:center;'>"
						+ "<h6 style='color:"+Colour.GENERIC_EXPERIENCE.toWebHexString()+"; text-align:center;'>Jobs</h6>"
							+"<div class='container-full-width' style='margin-bottom:0;'>"
								+ "<div style='width:15%; float:left; font-weight:bold; margin:0; padding:0;'>"
									+ "Job"
								+ "</div>"
								+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b>Workers</b>"
								+"</div>"
								+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>Affection</b>"
								+"</div>"
								+ "<div style='float:left; width:15%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience</b>"
								+"</div>"
								+ "<div style='float:left; width:35%; font-weight:bold; margin:0; padding:0;'>"
									+ "<b style='color:"+Colour.CURRENCY_GOLD.toWebHexString()+";'>Income</b>"
											+ " (+<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>Obedience Bonus</b>)"
								+"</div>"
								+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
									+ "Actions"
								+"</div>"
							+ "</div>");
			
			for(SlaveJob job : SlaveJob.values()) {
				affectionChange = job.getAffectionGain(character);
				obedienceChange = job.getObedienceGain(character);
				int income = job.getFinalHourlyIncomeAfterModifiers(character);
				boolean isCurrentJob = character.getSlaveJob() == job;
				
				UtilText.nodeContentSB.append(
						"<div class='container-full-width inner' "+(isCurrentJob?"style='background:#292929;'":"")+">"
							+"<div style='width:15%; float:left; margin:0; padding:0;'>"
								+ (isCurrentJob
									? "[style.boldGood("+job.getName(character)+")]"
									: (job.isAvailable(character)
										?job.getName(character)
										:"[style.colourBad("+job.getName(character)+")]"))
							+ "</div>"
							+ "<div style='float:left; width:10%; font-weight:bold; margin:0; padding:0;'>"
								+ Main.game.getPlayer().getSlavesWorkingJob(job)+"/"+(job.getSlaveLimit()<0?"&#8734;":job.getSlaveLimit())
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ (affectionChange>0
										?"<b style='color:"+Colour.AFFECTION.toWebHexString()+";'>+"+decimalFormat.format(affectionChange)+ "</b>"
										:(affectionChange<0
												?"<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+decimalFormat.format(affectionChange)+ "</b>"
												:"[style.colourDisabled(0)]"))+"/hour"
							+"</div>"
							+ "<div style='float:left; width:15%; margin:0; padding:0;'>"
								+ (obedienceChange>0
										?"<b style='color:"+Colour.OBEDIENCE.toWebHexString()+";'>+"+decimalFormat.format(obedienceChange)+ "</b>"
										:(obedienceChange<0
												?"<b style='color:"+Colour.GENERIC_BAD.toWebHexString()+";'>"+decimalFormat.format(obedienceChange)+ "</b>"
												:"[style.colourDisabled(0)]"))+"/hour"
							+"</div>"
							+ "<div style='float:left; width:35%; margin:0; padding:0;'>"
								+ UtilText.formatAsMoney(job.getIncome())
								+ " + ("
								+ (job.getObedienceIncomeModifier()>0
										?"[style.colourObedience("+job.getObedienceIncomeModifier()+")]"
										:"[style.colourDisabled("+job.getObedienceIncomeModifier()+")]")
										+ "*<span style='color:"+obedience.getColour().toWebHexString()+";'>"+character.getObedienceValue()+"</span>)"
								+ " = "+UtilText.formatAsMoney(income, "b", (income>0?Colour.TEXT:Colour.GENERIC_BAD))+"/hour"
							+"</div>"
							+ "<div style='float:left; width:10%; margin:0; padding:0;'>"
								+ (!job.isAvailable(character) || isCurrentJob
										?"<div id='"+job+"_ASSIGN_DISABLED' class='square-button solo"+(!isCurrentJob?" disabled":"")+"'><div class='square-button-content'>"
											+(isCurrentJob?SVGImages.SVG_IMAGE_PROVIDER.getResponseOption():SVGImages.SVG_IMAGE_PROVIDER.getResponseOptionDisabled())+"</div></div>"
										:"<div id='"+job+"_ASSIGN' class='square-button solo'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseOptionDisabled()+"</div></div>")
							+"</div>"
							+ "<div class='container-full-width' style='background:transparent; margin:0;'>"
								+ "<i>"
									+(isCurrentJob?job.getDescription():"[style.colourDisabled("+job.getDescription()+")]")
									+ "</br>"
									+ (!isCurrentJob && !job.isAvailable(character)?"[style.colourBad("+job.getAvailabilityText(character)+")]":"")
								+"</i>"
							+ "</div>");
				
				// Job Settings:
				for(SlaveJobSetting setting : job.getMutualSettings()) {
					boolean settingActive = character.getSlaveJobSettings().contains(setting);
					
					UtilText.nodeContentSB.append("<div class='container-full-width inner' style='"+(!isCurrentJob?"background:#1B1B1B;":"")+"'>"
													+"<div style='width:20%; float:left; margin:0; padding:0;"+(!isCurrentJob?"color:#777;":(settingActive?"color:"+Colour.GENERIC_GOOD.toWebHexString()+";":""))+"'>"
														+ setting.getName()
													+ "</div>"
													+"<div style='width:70%; float:left; margin:0; padding:0;"+(!settingActive?"color:#777;":"")+"'>"
														+ "<i>"+setting.getDescription()+"</i>"
													+ "</div>"
													+ "<div style='float:left; width:10%; margin:0; padding:0;'>"
														+ (!isCurrentJob
																?"<div id='"+setting+"_DISABLED' class='square-button solo disabled'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseUnlockedDisabled()+"</div></div>"
																: (settingActive
																		?"<div id='"+setting+"_REMOVE' class='square-button solo'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseUnlocked()+"</div></div>"
																		:"<div id='"+setting+"_ADD' class='square-button solo'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseUnlockedDisabled()+"</div></div>"))
													+"</div>"
												+ "</div>");
				}
				
				//TODO mutEx settings:
				
				UtilText.nodeContentSB.append("</div>");
			}
			UtilText.nodeContentSB.append("</div>");
			
			UtilText.nodeContentSB.append("<p id='hiddenFieldName' style='display:none;'></p>");
			return UtilText.parse(character, UtilText.nodeContentSB.toString());
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 7) {
				return new Response("Jobs", "You are already viewing the jobs screen.", null);
				
			}
			
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
		
		@Override
		public boolean reloadOnRestore() {
			return true;
		}
	};
	
	public static final DialogueNodeOld SLAVE_MANAGEMENT_PERMISSIONS = new DialogueNodeOld("Slave Management", ".", true) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getLabel() {
			return UtilText.parse(Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected(), "[npc.Name] - Permissions");
		}
		
		@Override
		public String getContent() {
			NPC character = Main.game.getDialogueFlags().getSlaveryManagerSlaveSelected();
			
			UtilText.nodeContentSB.setLength(0);
			
			UtilText.nodeContentSB.append(getSlaveInformationHeader(character));
			
			// Permissions:
			UtilText.nodeContentSB.append(
					"<div class='container-full-width' style='text-align:center;'>"
						+ "<h6 style='color:"+Colour.GENERIC_ARCANE.toWebHexString()+"; text-align:center;'>Permissions</h6>");
			
			for(SlavePermission permission : SlavePermission.values()) {
				UtilText.nodeContentSB.append(
						"<div class='container-full-width inner' style='background:#292929;'>"
								+ "<h6 style='color:"+permission.getColour().toWebHexString()+"; text-align:center;'>"+permission.getName()+"</h6>");
				
				// Job Settings:
				for(SlavePermissionSetting setting : permission.getSettings()) {
					boolean settingActive = character.getSlavePermissionSettings().get(permission).contains(setting);
					
					UtilText.nodeContentSB.append("<div class='container-full-width inner'>"
													+"<div style='width:20%; float:left; margin:0; padding:0;"+(settingActive?"color:"+Colour.GENERIC_GOOD.toWebHexString()+";":"")+"'>"
														+ setting.getName()
													+ "</div>"
													+"<div style='width:70%; float:left; margin:0; padding:0;"+(!settingActive?"color:#777;":"")+"'>"
														+ "<i>"+setting.getDescription()+"</i>"
													+ "</div>"
													+ "<div style='float:left; width:10%; margin:0; padding:0;'>"
														+ (permission.isMutuallyExclusiveSettings()
																? (settingActive
																	?"<div id='"+setting+"_REMOVE_ME' class='square-button huge'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseOption()+"</div></div>"
																	:"<div id='"+setting+"_ADD' class='square-button huge'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseOptionDisabled()+"</div></div>")
																:(settingActive
																		?"<div id='"+setting+"_REMOVE' class='square-button huge'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseUnlocked()+"</div></div>"
																		:"<div id='"+setting+"_ADD' class='square-button huge'><div class='square-button-content'>"+SVGImages.SVG_IMAGE_PROVIDER.getResponseUnlockedDisabled()+"</div></div>"))
													+"</div>"
												+ "</div>");
				}
				
				UtilText.nodeContentSB.append("</div>");
			}
			UtilText.nodeContentSB.append("</div>");
			
			return UtilText.parse(character, UtilText.nodeContentSB.toString());
		}

		@Override
		public Response getResponse(int responseTab, int index) {
			if (index == 8) {
				return new Response("Permissions", "You are already viewing the permissions screen.", null);
				
			}
			
			return getSlaveryResponse(index);
		}
		
		@Override
		public boolean isMapDisabled() {
			return true;
		}
		
		@Override
		public boolean reloadOnRestore() {
			return true;
		}
	};
}
