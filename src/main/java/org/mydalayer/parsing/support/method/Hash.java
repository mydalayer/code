package org.mydalayer.parsing.support.method;

import java.util.List;

import org.mydalayer.util.DALUtils;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * 用法:hash(main_name,route_param,table_number)
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class Hash implements TemplateMethodModelEx {

	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() != 3L) {
			throw new TemplateModelException(
					"the field number of  tableRouteMethod hash(main_name,route_param,table_number)  is wrong");
		}

		String tableName;
		String paramValue;
		int number;
		try {

			tableName = arguments.get(0).toString();
			paramValue = arguments.get(1).toString();
			number = Integer.valueOf(arguments.get(2).toString());

			if (tableName.equals("")) {
				throw new TemplateModelException(
						"main_name in tableRouteMethod hash(main_name,route_param,table_number) is null");
			}

			if (paramValue.equals("")) {
				throw new TemplateModelException(
						"can not get route_param in tableRouteMethod hash(main_name,route_param,table_number) "
								+ "from paramMap");
			}
			long hashCode = DALUtils.HashAlgorithm.KETAMA_HASH.hash(
					DALUtils.computeMd5(paramValue), 0);
			tableName = tableName + hashCode % number;
			return tableName.toUpperCase();

		} catch (NumberFormatException e) {
			throw new TemplateModelException(
					"field type error in tableRouteMethod hash(main_name,route_param,table_number)");
		}

	}

}
