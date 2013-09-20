/**
 * SIDCLookup.java
 * 
 * Revision History:<br>
 * Oct 1, 2008 joshua - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package edu.tamu.deepGreen;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;

/**
 * Simple class to look up the SIDC for the shapes implemented for the November
 * test. This class will be depracated and should not be relied upon...
 * 
 * @author joshua
 */
@Deprecated
public class SIDCLookup {
	
	/**
	 * Logger for this class
	 */
	private static Logger log = LadderLogger.getLogger(SIDCLookup.class);
	
	/**
	 * Map that contains all the SIDCs for the symbols we recognize. This is a
	 * stupid way to do this.
	 */
	protected static HashMap<String, String> S_SIDC_MAP = new HashMap<String, String>();
	static {

				S_SIDC_MAP.put("actionPoint", "GFGPGPP--------");
				S_SIDC_MAP.put("airDefense", "SFGPUCD----F---");
				S_SIDC_MAP.put("antiArmor",  "SFGPUCAA---F---");
				S_SIDC_MAP.put("antiArmorAirAssault", "SFGPUCAAS--F---");
				S_SIDC_MAP.put("antiArmorAirborne", "SFGPUCAAM--F---");
				S_SIDC_MAP.put("antiArmorArmoredAirAssault", "SFGPUCAAAS-F---");
				S_SIDC_MAP.put("antiArmorArmoredWheeled", "SFGPUCAAAW-F---");
				S_SIDC_MAP.put("antiArmorLight", "SFGPUCAAL--F---");
				S_SIDC_MAP.put("antiArmorMotorized", "SFGPUCAAO--F---");
				S_SIDC_MAP.put("antiArmorMotorizedAirAssault", "SFGPUCAAOS-F---");
				S_SIDC_MAP.put("armor", "SFGPUCA----F---");	
				S_SIDC_MAP.put("armorTrackedAirborne", "SFGPUCATA--F---");
				S_SIDC_MAP.put("armorTrackedAmphibious", "SFGPUCATW--F---");
				S_SIDC_MAP.put("armorTrackedHeavy","SFGPUCATH--F---");	
				S_SIDC_MAP.put("armorTrackedLight", "SFGPUCATL--F---");
				S_SIDC_MAP.put("armorTrackedMedium", "SFGPUCATM--F---");
				S_SIDC_MAP.put("armorWheeled", "SFGPUCAW--F---");
				S_SIDC_MAP.put("armorWheeledAirborne", "SFGPUCAWA-F---");
				S_SIDC_MAP.put("armorWheeledAmphibious", "SFGPUCAWW--F---");
				S_SIDC_MAP.put("armorWheeledHeavy", "SFGPUCAWH--F---");
				S_SIDC_MAP.put("armorWheeledLight", "SFGPUCAWL--F---");
				S_SIDC_MAP.put("armorWheeledMedium", "SFGPUCAWM--F---");
				S_SIDC_MAP.put("aviation", "SFGPUCV----F---");
				S_SIDC_MAP.put("aviationRotaryWingAttack","SFGPUCVRA--F---");
				S_SIDC_MAP.put("aviationRotaryWingAttackAlternate", "SFGPUCVRA--F---");
				S_SIDC_MAP.put("aviationRotaryWingUtility", "SFGPUCVRU--F---");
				S_SIDC_MAP.put("breach", "G-TPZ----------");
				S_SIDC_MAP.put("bridge", "G-MPBCB--------");
				S_SIDC_MAP.put("combatServiceSupportSupplyTrains", "SFGPUSSH---F---");	
				S_SIDC_MAP.put("combatSupportMilitaryIntelligence", "SFGPUUM----F---");
				S_SIDC_MAP.put("coordinationPoint", "GFGPGPPO---F---");	
				S_SIDC_MAP.put("engineer", "SFGPUCE----F---");
				S_SIDC_MAP.put("engineerCombatAirAssault", "SFGPUCECS--F---");
				S_SIDC_MAP.put("engineerCombatAirborne", "SFGPUCECA--F---");
				S_SIDC_MAP.put("engineerCombatMechanized", "SFGPUCECT--F---");
				S_SIDC_MAP.put("engineerCombatMotorized", "SFGPUCECW--F---");
				S_SIDC_MAP.put("engineerCombatReconnaissance", "SFGPUCECR--F---");
				S_SIDC_MAP.put("defeat", "GFTPE------F---");
				S_SIDC_MAP.put("fieldArtillery", "SFGPUCF----F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerAirAssault", "SFGPUCFHS--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerAirborne", "SFGPUCFHA--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerAmphibious", "SFGPUCFHX--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerHeavy", "SFGPUCFHH--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerLight", "SFGPUCFHL--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerMedium", "SFGPUCFHM--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerRocketMultiRocketLauncher", "SFGPUCFRM--F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerRocketMultiRocketLauncherMultiRocketTruck", "SFGPUCFRMR-F---");		
				S_SIDC_MAP.put("fieldArtilleryHowitzerRocketMultiRocketLauncherSelfPropelled", "SFGPUCFRMS-F---");
				S_SIDC_MAP.put("fieldArtilleryHowitzerSelfPropelled", "SFGPUCFHE--F---");
				S_SIDC_MAP.put("fieldArtilleryMortarSelfPropelledTracked", "SFGPUCFMS--F---");
				S_SIDC_MAP.put("fieldArtilleryMortar", "SFGPUCR----F---");
				S_SIDC_MAP.put("fieldArtilleryMortarAmphibious", "SFGPUCFML--F---");
				S_SIDC_MAP.put("hostileAirDefense", "SHGPUCD----F---");
				S_SIDC_MAP.put("hostileAntiArmor", "SHGPUCAA---F---");
				S_SIDC_MAP.put("hostileAntiArmorAirAssault", "SHGPUCAAS--F---");
				S_SIDC_MAP.put("hostileAntiArmorAirborne", "SHGPUCAAM--F---");
				S_SIDC_MAP.put("hostileAntiArmorArmoredAirAssault", "SHGPUCAAAS-F---");
				S_SIDC_MAP.put("hostileAntiArmorArmoredWheeled", "SHGPUCAAAW-F---");
				S_SIDC_MAP.put("hostileAntiArmorLight", "SHGPUCAAL--F---");
				S_SIDC_MAP.put("hostileAntiArmorMotorized", "SHGPUCAAO--F---");
				S_SIDC_MAP.put("hostileAntiArmorMotorizedAirAssault", "SFGPUCAAOS-F---");
				S_SIDC_MAP.put("hostileArmor", "SHGPUCA----F---");	
				S_SIDC_MAP.put("hostileArmorTrackedAirborne", "SHGPUCATA--F---");
				S_SIDC_MAP.put("hostileArmorTrackedAmphibious", "SHGPUCATW--F---");
				S_SIDC_MAP.put("hostileArmorTrackedHeavy", "SHGPUCATH--F---");		
				S_SIDC_MAP.put("hostileArmorTrackedLight","SHGPUCATL--F---");
				S_SIDC_MAP.put("hostileArmorTrackedMedium", "SHGPUCATM--F---");
				S_SIDC_MAP.put("hostileArmorWheeled", "SHGPUCAW---F---");
				S_SIDC_MAP.put("hostileArmorWheeledAirborne","SHGPUCAWA--F---");
				S_SIDC_MAP.put("hostileArmorWheeledAmphibious", "SHGPUCAWW--F---");
				S_SIDC_MAP.put("hostileArmorWheeledHeavy", "SHGPUCAWH--F---");
				S_SIDC_MAP.put("hostileArmorWheeledLight", "SHGPUCAWL--F---");
				S_SIDC_MAP.put("hostileArmorWheeledMedium","SHGPUCAWM--F---");
				S_SIDC_MAP.put("hostileAviation", "SHGPUCV----F---");
				S_SIDC_MAP.put("hostileAviationRotaryWingAttack","SHGPUCVRA--F---");
				S_SIDC_MAP.put("hostileAviationRotaryWingAttackAlternate", "SHGPUCVRA--F---");
				S_SIDC_MAP.put("hostileAviationRotaryWingUtility", "SHGPUCVRU--F---");
				S_SIDC_MAP.put("hostileCombatSupportMilitaryIntelligence", "SHGPUUM----F---");
				S_SIDC_MAP.put("hostileEngineer", "SHGPUCE----F---");
				S_SIDC_MAP.put("hostileEngineerCombatAirAssault", "SHGPUCECS--F---");
				S_SIDC_MAP.put("hostileEngineerCombatAirborne", "SHGPUCECA--F---");
				S_SIDC_MAP.put("hostileEngineerCombatMechanized", "SHGPUCECT--F---");
				S_SIDC_MAP.put("hostileEngineerCombatMotorized", "SHGPUCECW--F---");
				S_SIDC_MAP.put("hostileEngineerCombatReconnaissance", "SHGPUCECR--F---");
				S_SIDC_MAP.put("hostileFieldArtillery", "SHGPUCF----F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerAirAssault", "SHGPUCFHS--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerAirborne", "SHGPUCFHA--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerAmphibious", "SHGPUCFHX--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerHeavy", "SHGPUCFHH--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerLight", "SHGPUCFHL--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerMedium", "SHGPUCFHM--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerRocketMultiRocketLauncher", "SHGPUCFRM--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerRocketMultiRocketLauncherMultiRocketTruck", "SHGPUCFRMR-F---");	
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerRocketMultiRocketLauncherSelfPropelled", "SHGPUCFRMS-F---");
				S_SIDC_MAP.put("hostileFieldArtilleryHowitzerSelfPropelled", "SHGPUCFHE--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryMortar", "SHGPUCR----F---");
				S_SIDC_MAP.put("hostileFieldArtilleryMortarAmphibious", "SHGPUCFML--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryMortarSelfPropelledTracked", "SFGPUCFMS--F---");
				S_SIDC_MAP.put("hostileFieldArtilleryMortarSelfPropelledWheeled", "SHGPUCFMW--F---");
				S_SIDC_MAP.put("hostileInfantry", "SHGPUCI----F---");
				S_SIDC_MAP.put("hostileInfantryAirAssault", "SHGPUCIS---F---");
				S_SIDC_MAP.put("hostileInfantryAirborne", "SHGPUCIA---F---");
				S_SIDC_MAP.put("hostileInfantryInfantryFightingVehicle", "SHGPUCII---F---");
				S_SIDC_MAP.put("hostileInfantryLight", "SHGPUCIL---F---");
				S_SIDC_MAP.put("hostileInfantryMechanized", "SHGPUCIZ---F---");
				S_SIDC_MAP.put("hostileInfantryMotorized", "SHGPUCIM---F---");
				S_SIDC_MAP.put("hostileReconnaissance", "SHGPUCRV---F---");
				S_SIDC_MAP.put("hostileReconnaissanceAirAssault", "SHGPUCRS---F---");
				S_SIDC_MAP.put("hostileReconnaissanceAirborne", "SHGPUCRA---F---");
				S_SIDC_MAP.put("hostileReconnaissanceCavalryAir", "SHGPUCRVO--F---");
				S_SIDC_MAP.put("hostileReconnaissanceCavalryArmored", "SHGPUCRVA--F---");
				S_SIDC_MAP.put("hostileReconnaissanceCavalryMotorized", "SHGPUCRVM--F---");
				S_SIDC_MAP.put("hostileReconnaissanceLight", "SHGPUCRL---F---");
				S_SIDC_MAP.put("hostileReconnaissanceMarine", "SHGPUCRR---F---");
				S_SIDC_MAP.put("hostileReconnaissanceMarineForce", "SHGPUCRRF--F---");
				S_SIDC_MAP.put("hq", "SFGPUH----A----");	
				S_SIDC_MAP.put("infantry", "SFGPUCI----F---");
				S_SIDC_MAP.put("infantryAirAssault","SFGPUCIS---F---");
				S_SIDC_MAP.put("infantryAirborne", "SFGPUCIA---F---");
				S_SIDC_MAP.put("infantryInfantryFightingVehicle", "SFGPUCII---F---");
				S_SIDC_MAP.put("infantryLight", "SFGPUCIL---F---");
				S_SIDC_MAP.put("infantryMechanized", "SFGPUCIZ---F---");
				S_SIDC_MAP.put("infantryMotorized", "SFGPUCIM---F---");
				S_SIDC_MAP.put("maneuverOffenseAttackByFirePosition", "GFGPOAF----F---");
				S_SIDC_MAP.put("maneuverOffenseDirectionOfAttackGroundSupportAttack", "GFGPOAG----F---");		
				S_SIDC_MAP.put("obstacle", "GFMPBCL--------");	
				S_SIDC_MAP.put("obstacleAntitankDitchComplete", "GFMPOADC---F---");
				S_SIDC_MAP.put("obstacleEffectBlock", "GFMPOEB----F---");
				S_SIDC_MAP.put("obstacleMinefieldStatic", "GFMPOFS----F---");
				S_SIDC_MAP.put("occupy", "GFTPO------F---");	
				S_SIDC_MAP.put("penetrate", "GFTPP------F---");	
				S_SIDC_MAP.put("reconnaissance", "SFGPUCRV---F---");
				S_SIDC_MAP.put("reconnaissanceAirAssault", "SFGPUCRS---F---");
				S_SIDC_MAP.put("reconnaissanceAirborne", "SFGPUCRA---F---");
				S_SIDC_MAP.put("reconnaissanceCavalryAir", "SFGPUCRVO--F---");
				S_SIDC_MAP.put("reconnaissanceCavalryArmored", "SFGPUCRVA--F---");
				S_SIDC_MAP.put("reconnaissanceCavalryMotorized", "SFGPUCRVM--F---");
				S_SIDC_MAP.put("reconnaissanceLight", "SFGPUCRL---F---");
				S_SIDC_MAP.put("reconnaissanceMarine", "SFGPUCRR---F---");
				S_SIDC_MAP.put("reconnaissanceMarineForce", "SFGPUCRRF--F---");
				S_SIDC_MAP.put("secure", "GFTPS------F---");
						
	}
	
	
	/**
	 * Get the SIDC for the given shape label.
	 * 
	 * @param shapeLabel
	 *            THe shape label from the shape definition
	 * @return The SIDC that matches the shape label.
	 */
	public static String getSIDC(String shapeLabel) {
		log.debug("Get SIDC for "+shapeLabel);
		return S_SIDC_MAP.get(shapeLabel);
	}
}
