package com.mrprez.gencross.impl.changelin;

import com.mrprez.gencross.Property;
import com.mrprez.gencross.renderer.Renderer;
import com.mrprez.gencross.value.Value;

public class MdTRenderer extends Renderer {

	@Override
	public String displayValue(Value value) {
		StringBuilder stringBuilder = new StringBuilder("<html>");
		for(int i=0; i<value.getInt(); i++){
			stringBuilder.append("&#9679;");
		}
		for(int i=value.getInt(); i<5; i++){
			stringBuilder.append("&#9675;");
		}
		stringBuilder.append("</html>");
		return stringBuilder.toString();
	}

	@Override
	public String displayValue(Property property) {
		StringBuilder stringBuilder = new StringBuilder("<html>");
		for(int i=0; i<property.getValue().getInt(); i++){
			stringBuilder.append("&#9679;");
		}
		int max = 5;
		if(property.getMax()!=null){
			max = property.getMax().getInt();
		}
		for(int i=property.getValue().getInt(); i<max; i++){
			stringBuilder.append("&#9675;");
		}
		stringBuilder.append("</html>");
		return stringBuilder.toString();
	}
	
	

}
