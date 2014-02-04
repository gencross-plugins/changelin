package com.mrprez.gencross.impl.changelin;

import com.mrprez.gencross.Property;
import com.mrprez.gencross.Version;
import com.mrprez.gencross.migration.DummyRenderer;
import com.mrprez.gencross.migration.MigrationPersonnage;
import com.mrprez.gencross.migration.Migrator;

public class MigrationTo1_2 implements Migrator {

	@Override
	public MigrationPersonnage migrate(MigrationPersonnage migrationPersonnage) throws Exception {
		for(Property property : migrationPersonnage.getProperties()){
			changeRenderer(property);
		}
		
		migrationPersonnage.getPluginDescriptor().setVersion(new Version(1,2));
		return migrationPersonnage;
	}
	
	private void changeRenderer(Property property){
		if(property.getRenderer() instanceof DummyRenderer){
			DummyRenderer renderer = (DummyRenderer) property.getRenderer();
			if(renderer.getClassName().equals("com.mrprez.gencross.impl.mdt.MdTRenderer")){
				renderer.setClassName("com.mrprez.gencross.impl.changelin.MdTRenderer");
			}
		}
		if(property.getSubProperties() != null){
			for(Property subProperty : property.getSubProperties()){
				changeRenderer(subProperty);
			}
			for(Property option : property.getSubProperties().getOptions().values()){
				changeRenderer(option);
			}
			if(property.getSubProperties().getDefaultProperty() != null){
				changeRenderer(property.getSubProperties().getDefaultProperty());
			}
		}
	}

}
