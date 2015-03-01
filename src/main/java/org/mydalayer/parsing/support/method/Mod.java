package org.mydalayer.parsing.support.method;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * 用法:mod(main_name,route_param,table_number)
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class Mod implements TemplateMethodModelEx {

	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List args) throws TemplateModelException {
		if (args.size() != 3L) {
			throw new TemplateModelException(
					"the field number of  tableRouteMethod mod(main_name,route_param,table_number)  is wrong");
		}

		String tableName;
		long paramValue;
		int number;
		try {

			tableName = args.get(0).toString();
			paramValue = Long.valueOf(args.get(1).toString());
			number = Integer.valueOf(args.get(2).toString());

			if (tableName.equals("")) {
				throw new TemplateModelException(
						"main_name in tableRouteMethod mod(main_name,route_param,table_number) is null");
			}
			tableName = tableName + paramValue % number;
			return tableName.toUpperCase();

		} catch (NumberFormatException e) {
			throw new TemplateModelException(
					"field type error in tableRouteMethod mod(main_name,route_param,table_number)");
		}

	}
}
