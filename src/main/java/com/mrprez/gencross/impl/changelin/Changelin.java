package com.mrprez.gencross.impl.changelin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.PoolPoint;
import com.mrprez.gencross.PropertiesList;
import com.mrprez.gencross.Property;
import com.mrprez.gencross.history.ConstantHistoryFactory;
import com.mrprez.gencross.history.HistoryItem;
import com.mrprez.gencross.history.HistoryUtil;
import com.mrprez.gencross.history.LevelToReachHistoryFactory;
import com.mrprez.gencross.history.ProportionalHistoryFactory;
import com.mrprez.gencross.value.IntValue;
import com.mrprez.gencross.value.StringValue;
import com.mrprez.gencross.value.Value;

public class Changelin extends Personnage {
	private static List<String> saisonList = Arrays.asList("Printemps", "Eté", "Automne", "Hiver");
	private static List<String> universalContracts = Arrays.asList("Mirroir", "Rêves", "Foyer", "Brumes");
	
	
	
	@Override
	public void calculate() {
		super.calculate();
		if(phase.equals("Seeming")){
			if(getProperty("Seeming").getValue().getString().isEmpty()){
				errors.add("Vous devez choisir un \"Seeming\"");
			}
		}
		if(phase.equals("Création")){
			calculateAttributs();
			calculateTalents();
			calculatePointPools();
			calculateViceEtVertu();
			calculateAvantages();
			calculateCourtContrat();
			calculateSeemingAndCourtContrats();
			calculateCourt();
			calculateSpecialite();
		}
	}
	
	@Override
	public void passToNextPhase() throws Exception {
		super.passToNextPhase();
		if(phase.equals("Création")){
			getProperty("Contrats").getSubProperties().setFixe(false);
			getProperty("Seeming").setEditable(false);
			Property kithProperty = getProperty("Kith");
			for(int i=1; appendix.containsKey("kith."+getProperty("Seeming").getValue()+"."+i); i++){
				kithProperty.getOptions().add(new StringValue(appendix.getProperty("kith."+getProperty("Seeming").getValue()+"."+i)));
			}
		}
		if(phase.equals("En vie")){
			getProperty("Kith").setEditable(false);
			getProperty("Vertu").setEditable(false);
			getProperty("Vice").setEditable(false);
			Property attributs = getProperty("Attributs");
			for(Property attributGroup : attributs.getSubProperties()){
				attributGroup.setEditable(false);
				for(Property attribut : attributGroup.getSubProperties()){
					attribut.setHistoryFactory(new LevelToReachHistoryFactory(5, "Expérience"));
				}
			}
			Property talents = getProperty("Talents");
			for(Property talentGroup : talents.getSubProperties()){
				talentGroup.setEditable(false);
				for(Property talent : talentGroup.getSubProperties()){
					talent.setHistoryFactory(new LevelToReachHistoryFactory(3, "Expérience"));
					talent.getSubProperties().getDefaultProperty().setHistoryFactory(new ConstantHistoryFactory("Expérience", 3));
				}
			}
			Property avantages = getProperty("Avantages");
			avantages.getSubProperties().getDefaultProperty().setHistoryFactory(new LevelToReachHistoryFactory(2, "Expérience"));
			for(Property avantage : avantages.getSubProperties()){
				avantage.setHistoryFactory(new LevelToReachHistoryFactory(2, "Expérience"));
			}
			for(Property option : avantages.getSubProperties().getOptions().values()){
				option.setHistoryFactory(new LevelToReachHistoryFactory(2, "Expérience"));
			}
			getProperty("Wyrd").setHistoryFactory(new LevelToReachHistoryFactory(8, "Expérience"));
			
			Set<String> affinityContract = new HashSet<String>();
			affinityContract.addAll(universalContracts);
			String seemingContractName = appendix.getProperty("contract."+getProperty("Seeming").getValue().getString());
			affinityContract.add(seemingContractName);
			String court = getCourt();
			if(court!=null){
				affinityContract.add(court+" éternel");
				affinityContract.add(court+" fugace");
			}
			Property contrats = getProperty("Contrats");
			for(Property contract : contrats.getSubProperties()){
				if(affinityContract.contains(contract.getName())){
					contract.setHistoryFactory(new LevelToReachHistoryFactory(4, "Expérience"));
				}else if(contract.getName().startsWith("Gobelin")){
					contract.setHistoryFactory(new ProportionalHistoryFactory("Expérience", 3));
				}else{
					contract.setHistoryFactory(new LevelToReachHistoryFactory(6, "Expérience"));
				}
			}
			for(Property contractOption : contrats.getSubProperties().getOptions().values()){
				if(affinityContract.contains(contractOption.getName())){
					contractOption.setHistoryFactory(new LevelToReachHistoryFactory(4, "Expérience"));
				}else if(contractOption.getName().startsWith("Gobelin")){
					contractOption.setHistoryFactory(new ProportionalHistoryFactory("Expérience", 3));
				}else{
					contractOption.setHistoryFactory(new LevelToReachHistoryFactory(6, "Expérience"));
				}
			}
			
			formulaManager.removeFormula("Volonté");
			getProperty("Volonté").setHistoryFactory(new ProportionalHistoryFactory("Expérience", 8));
			getProperty("Volonté").setEditable(true);
			getProperty("Clareté").setHistoryFactory(new LevelToReachHistoryFactory(3, "Expérience"));
			
			
		}
	}


	private void calculateSeemingAndCourtContrats(){
		String seeming = getProperty("Seeming").getValue().getString();
		String seemingContrat = "Aucun Contrat de Seeming";
		if(seeming.equals("Beast")){
			seemingContrat = "Griffes et Crocs";
		}else if(seeming.equals("Elemental")){
			seemingContrat = "Eléments";
		}else if(seeming.equals("Fairest")){
			seemingContrat = "Vaine Gloire";
		}else if(seeming.equals("Ogre")){
			seemingContrat = "Pierre";
		}else if(seeming.equals("Darkling")){
			seemingContrat = "Ombres";
		}else if(seeming.equals("Wizened")){
			seemingContrat = "Artifice";
		}
		String court = "Sans court";
		PropertiesList avantages = getProperty("Avantages").getSubProperties();
		for(String saison : saisonList){
			if(avantages.get("Court ("+saison+")")!=null){
				court = saison;
			}
		}
		
		int points = HistoryUtil.sumHistoryOfSpecifiableProperty(history, "Contrats", seemingContrat, "Contrats");
		points = points + HistoryUtil.sumHistory(history, "Contrats#"+court+" fugace", "Contrats");
		points = points + HistoryUtil.sumHistory(history, "Contrats#"+court+" éternel", "Contrats");
		
		if(points<2){
			errors.add("Vous devez dépenser un moins 2 points dans vos contrats de Seeming ou de Court");
		}
		
	}
	
	private void calculateCourt(){
		int nbCourt = 0;
		for(String saison : saisonList){
			if(getProperty("Avantages#Court ("+saison+")")!=null){
				nbCourt++;
			}
		}
		if(nbCourt>1){
			errors.add("Vous ne pouvez être rattaché à 2 Courts");
		}
	}
	
	private void calculateCourtContrat(){
		String saisonsTab[] = {"Printemps", "Eté", "Automne", "Hivers"};
		for(int i=0; i<4; i++){
			String saison = saisonsTab[i];
			if(getProperty("Contrats#"+saison+" fugace")!=null){
				int court = getProperty("Avantages#Court ("+saison+")")==null?0:getProperty("Avantages#Court ("+saison+")").getValue().getInt();
				int complaisance = getProperty("Avantages#Complaisances de court ("+saison+")")==null?0:getProperty("Avantages#Complaisances de court ("+saison+")").getValue().getInt();
				if(getProperty("Contrats#"+saison+" fugace").getValue().getInt()>court+1 && getProperty("Contrats#"+saison+" fugace").getValue().getInt()>complaisance-1){
					errors.add("Niveau de Court ou Complaisances de court insuffisant pour le contrat: "+saison+" fugace");
				}
			}
			if(getProperty("Contrats#"+saison+" éternel")!=null){
				int court = getProperty("Avantages#Court ("+saison+")")==null?0:getProperty("Avantages#Court ("+saison+")").getValue().getInt();
				int complaisance = getProperty("Avantages#Complaisances de court ("+saison+")")==null?0:getProperty("Avantages#Complaisances de court ("+saison+")").getValue().getInt();
				if(getProperty("Contrats#"+saison+" éternel").getValue().getInt()>court+1 && getProperty("Contrats#"+saison+" éternel").getValue().getInt()>complaisance-1){
					errors.add("Niveau de Court ou Complaisances de court insuffisant pour le contrat: "+saison+" éternel");
				}
			}
		}
	}
	
	private void calculateSpecialite(){
		if(getProperty("Talents#Physique#Athlétisme").getSubProperties().isEmpty() &&
				getProperty("Talents#Physique#Bagarre").getSubProperties().isEmpty() &&
				getProperty("Talents#Physique#Furtivité").getSubProperties().isEmpty()){
			errors.add("Vous devez avoir une spécialité en Athlétisme, Bagarre ou Furtivité en fonction de votre Seeming");
		}
	}
	
	public Boolean addCourt(Property court){
		String saison = court.getName().substring(court.getName().indexOf("(")+1, court.getName().length()-1);
		if(getProperty("Avantages#Complaisances de court ("+saison+")")!=null){
			actionMessage = "Vous avez déjà les complaisances de cette court";
			return false;
		}
		return true;
	}
	
	public Boolean addComplaisances(Property court){
		String saison = court.getName().substring(court.getName().indexOf("(")+1, court.getName().length()-1);
		if(getProperty("Avantages#Court ("+saison+")")!=null){
			actionMessage = "Vous faites déjà partie de cette court";
			return false;
		}
		return true;
	}

	private void calculateViceEtVertu(){
		if(getProperty("Vice").getValue().toString().equals("") || getProperty("Vertu").getValue().toString().equals("")){
			errors.add("Vous devez choisir une vertu et un vice");
		}
	}
	
	private void calculatePointPools(){
		String error = null;
		Iterator<PoolPoint> it = getPointPools().values().iterator();
		while(it.hasNext() && error==null){
			if(it.next().getRemaining()!=0){
				error = "Il reste des points à dépenser";
			}
		}
		if(error!=null){
			errors.add(error);
		}
	}
	
	private void calculateAttributs(){
		String mental = getProperty("Attributs#Mental").getValue().toString();
		String physique = getProperty("Attributs#Physique").getValue().toString();
		String social = getProperty("Attributs#Social").getValue().toString();
		if(mental.equals(physique) || mental.equals(social) || physique.equals(social)){
			errors.add("Vous devez hiérarchiser vos groupes d'Attributs");
		}
	}
	
	private void calculateTalents(){
		String mental = getProperty("Talents#Mental").getValue().toString();
		String physique = getProperty("Talents#Physique").getValue().toString();
		String social = getProperty("Talents#Social").getValue().toString();
		if(mental.equals(physique) || mental.equals(social) || physique.equals(social)){
			errors.add("Vous devez hiérarchiser vos groupes de Talents");
		}
	}
	
	private void calculateAvantages(){
		for(Property avantage : getProperty("Avantages").getSubProperties().getProperties().values()){
			String errorMessage = checkAvantage(avantage);
			if(errorMessage!=null){
				errors.add(avantage.getFullName()+": "+errorMessage);
			}
		}
	}
	
	public void changeAttributGroupValue(Property attributGroup, Value oldValue) throws Exception{
		String newPoolPoint = "Attributs "+attributGroup.getValue().toString();
		String oldPoolPoint = "Attributs "+oldValue.toString();
		int transfertCost = 0;
		for(Property attribut : attributGroup.getSubProperties().getProperties().values()){
			attribut.getHistoryFactory().setPointPool(newPoolPoint);
			transfertCost = transfertCost + attribut.getHistoryFactory().getCost(new IntValue(1), attribut.getValue(), HistoryItem.UPDATE);
			List<HistoryItem> subHistory = HistoryUtil.getSubHistory(history, attribut);
			for(HistoryItem subHistoryItem : subHistory){
				subHistoryItem.setPointPool(newPoolPoint);
			}
		}
		getPointPools().get(oldPoolPoint).spend(-transfertCost);
		getPointPools().get(newPoolPoint).spend(transfertCost);
	}
	
	public void changeTalentGroupValue(Property talentGroup, Value oldValue) throws Exception{
		String newPoolPoint = "Talents "+talentGroup.getValue().toString();
		String oldPoolPoint = "Talents "+oldValue.toString();
		int transfertCost = 0;
		for(Property talent : talentGroup.getSubProperties().getProperties().values()){
			talent.getHistoryFactory().setPointPool(newPoolPoint);
			transfertCost = transfertCost + talent.getHistoryFactory().getCost(new IntValue(0), talent.getValue(), HistoryItem.UPDATE);
			List<HistoryItem> subHistory = HistoryUtil.getSubHistory(history, talent);
			for(HistoryItem subHistoryItem : subHistory){
				subHistoryItem.setPointPool(newPoolPoint);
			}
		}
		getPointPools().get(oldPoolPoint).spend(-transfertCost);
		getPointPools().get(newPoolPoint).spend(transfertCost);
	}
	
	public Boolean addAvantage(Property avantage){
		String erreur = checkAvantage(avantage);
		if(erreur!=null){
			actionMessage = erreur;
			return false;
		}
		return true;
	}
	
	private int compteLangues(Property langue){
		int compte = 0;
		for(Property avantage : getProperty("Avantages").getSubProperties().getProperties().values()){
			if(avantage.getName().equals("Langue étrangère")){
				compte++;
			}
		}
		if(getProperty("Avantages").getSubProperty(langue.getFullName())==null){
			compte++;
		}
		return compte;
	}
	
	private String checkAvantage(Property avantage){
		if(avantage.getName().equals("Langue étrangère")){
			if(compteLangues(avantage)>getProperty("Attributs#Mental#Intelligence").getValue().getInt()){
				return "Vous ne pouvez avoir plus de Langues étrangères que votre Intelligence";
			}
		}
		if(avantage.getName().equals("Conscience de l’invisible")){
			if(getProperty("Attributs#Mental#Astuce").getValue().getInt()<2){
				return "Vous devez avoir Astuce à 2";
			}
		}
		if(avantage.getName().equals("Capacité pulmonaire")){
			if(getProperty("Talents#Physique#Athlétisme").getValue().getInt()<3){
				return "Vous devez avoir Athlétisme à 3";
			}
		}
		if(avantage.getName().equals("Cascadeur")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
		}
		if(avantage.getName().equals("Désarmer")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<2){
				return "Vous devez avoir Mélée à 2";
			}
		}
		if(avantage.getName().equals("Dos musclé")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
		}
		if(avantage.getName().equals("Esquive (armes blanches)")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<1){
				return "Vous devez avoir Mélée à 1";
			}
		}
		if(avantage.getName().equals("Esquive (mains nues)")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
			if(getProperty("Talents#Physique#Bagarre").getValue().getInt()<1){
				return "Vous devez avoir Bagarre à 1";
			}
		}
		if(avantage.getName().equals("Estomac en béton")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
		}
		if(avantage.getName().equals("Flingueur")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Armes à feu").getValue().getInt()<3){
				return "Vous devez avoir Armes à feu à 3";
			}
		}
		if(avantage.getName().equals("Guérison rapide")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<4){
				return "Vous devez avoir Vigueur à 4";
			}
		}
		if(avantage.getName().equals("Immunité innée")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
		}
		if(avantage.getName().equals("Réflexes rapides")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
		}
		if(avantage.getName().equals("Résistance aux toxines")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<3){
				return "Vous devez avoir Vigueur à 3";
			}
		}
		if(avantage.getName().equals("Santé de fer")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<3
					&& getProperty("Attributs#Mental#Résolution").getValue().getInt()<3){
				return "Vous devez avoir Vigueur ou Résolution à 3";
			}
		}
		if(avantage.getName().equals("Sprinteur")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
		}
		if(avantage.getName().equals("Style de combat : boxe")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<3){
				return "Vous devez avoir Force à 3";
			}
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
			if(getProperty("Talents#Physique#Bagarre").getValue().getInt()<2){
				return "Vous devez avoir Bagarre à 2";
			}
		}
		if(avantage.getName().equals("Style de combat : Kung-fu")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<2){
				return "Vous devez avoir Agilité à 2";
			}
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
			if(getProperty("Talents#Physique#Bagarre").getValue().getInt()<2){
				return "Vous devez avoir Bagarre à 2";
			}
		}
		if(avantage.getName().equals("Style de combat : deux armes blanches")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<3){
				return "Vous devez avoir Mélée à 3";
			}
		}
		if(avantage.getName().equals("Techniques de combat")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<2){
				return "Vous devez avoir Mélée à 2";
			}
		}
		if(avantage.getName().equals("Tir rapide")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
		}
		if(avantage.getName().equals("Tour de chauffe")){
			if(getProperty("Avantages#Réflexes rapides")==null || getProperty("Avantages#Réflexes rapides").getValue().getInt()<2){
				return "Vous devez avoir Réflexes rapides à 2";
			}
		}
		if(avantage.getName().equals("Inspiration")){
			if(getProperty("Attributs#Social#Présence").getValue().getInt()<4){
				return "Vous devez avoir Présence à 4";
			}
		}
		if(avantage.getName().equals("Renommée")){
			if(getProperty("Attributs#Mental#Astuce").getValue().getInt()<5){
				return "Vous devez avoir Astuce à 5";
			}
		}
		return null;
	}
	
	public String getCourt(){
		for(String saison : saisonList){
			if(getProperty("Avantages#Court ("+saison+")")!=null){
				return saison;
			}
		}
		return null;
	}

	
	

}
