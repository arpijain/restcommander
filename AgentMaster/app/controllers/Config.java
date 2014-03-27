/*  

Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import models.utils.DateUtils;

import org.lightj.task.asynchttp.UrlTemplate;
import org.lightj.util.StringUtil;

import play.mvc.Controller;
import resources.IUserDataDao;
import resources.IUserDataDao.DataType;
import resources.UserDataProvider;
import resources.command.ICommand;

/**
 * 
 * @author ypei
 *
 */
public class Config extends Controller {

	/**
	 * view configs
	 * @param configType
	 * @param configKey
	 */
	public static void viewConfigItem(String configType, String configKey) {
		try {
			DataType type = DataType.valueOf(configType.toUpperCase());
			String jsonResult = UserDataProvider.getUserDataDao().readData(type, configKey);;
			renderJSON(jsonResult);
		} catch (IOException e) {
			error(e);
		}
	}

	/**
	 * reload all config
	 * @param type
	 */
	public static void reloadConfig(String type) {

		try {
			UserDataProvider.reloadAllConfigs();
			renderJSON("Successful reload config with type " + type);
		} catch (Throwable t) {
			t.printStackTrace();
			renderJSON("Error occured in reloadConfig with type" + type);
		}

	}
	
	/**
	 * show all configs of a type
	 * @param dataType
	 */
	public static void showConfigs(String dataType, String alert) {
		
		String page = "showConfig";
		String topnav = "config";

		try {
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			List<String> cfgNames = UserDataProvider.getUserDataDao().listNames(dType);

			String lastRefreshed = DateUtils.getNowDateTimeStrSdsm();

			render(page, topnav, dataType, cfgNames, lastRefreshed, alert);

		}
		catch (Exception e) {
			e.printStackTrace();
			error(e);
		}
	}

	/**
	 * edit page
	 * @param dataType
	 */
	static final String NEW_CONFIG_NAME = "newConfig";
	public static void editConfig(String dataType, String configName) {

		String page = "editConfig";
		String topnav = "config";

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}

			String content = null;
			if (StringUtil.equalIgnoreCase(NEW_CONFIG_NAME, configName)) {
				// this is for new configuration
				content = "";
			}
			else {
				IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
				content = userDataDao.readData(DataType.valueOf(dataType.toUpperCase()), configName);
			}
			
			String alert = null;

			render(page, topnav, dataType, configName, content, alert);
		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * save after edit
	 * @param dataType
	 * @param content
	 */
	public static void editConfigUpdate(String dataType, String configName, String configNameNew, String content) {

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			
			if (StringUtil.equalIgnoreCase(NEW_CONFIG_NAME, configName)) {
				// new config
				configName = configNameNew;
			}

			IUserDataDao userDataDao = UserDataProvider.getUserDataDao();
			userDataDao.saveData(DataType.valueOf(dataType.toUpperCase()), configName, content);

			String alert = "Config was successfully updated at " + DateUtils.getNowDateTimeStrSdsm();

			// reload after
			UserDataProvider.reloadAllConfigs();
			
			redirect("Config.showConfigs", dataType, alert);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * delete a config
	 * @param dataType
	 * @param configName
	 */
	public static void deleteConfig(String dataType, String configName) {

		try {
			if (dataType == null) {
				renderJSON("configFile is NULL. Error occured in editConfig");
			}
			
			DataType dType = DataType.valueOf(dataType.toUpperCase());
			UserDataProvider.getUserDataDao().deleteData(dType, configName);
			
			String alert = String.format("%s was successfully deleted at %s ", configName, DateUtils.getNowDateTimeStrSdsm());

			// reload after
			UserDataProvider.reloadAllConfigs();
			
			redirect("Config.showConfigs", dataType, alert);

		} catch (Exception e) {
			e.printStackTrace();
			error(e);
		}

	}// end func

	/**
	 * show all configs
	 */
	public static void index() {
		
		redirect("Config.showConfigs", "command");

	}

}
