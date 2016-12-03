/**
 * Copyright 2016-2016 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.rasc.piwik.tracking;

public enum QueryParameter {

	/** Recommended parameters */
	ACTION_NAME("action_name"), 
	VISITOR_ID("_id"),
	
	/** Optional User info */
	REFERRER_URL("urlref"),
	VISIT_CUSTOM_VARIABLE("_cvar"),
	VISITOR_VISIT_COUNT("_idvc"),
	VISITOR_PREVIOUS_VISIT_TIMESTAMP("_viewts"),
	VISITOR_FIRST_VISIT_TIMESTAMP("_idts"),
	CAMPAIGN_NAME("_rcn"),	
	CAMPAIGN_KEYWORD("_rck"),	
	DEVICE_RESOLUTION("res"),
	CURRENT_HOUR("h"),
	CURRENT_MINUTE("m"), 
	CURRENT_SECOND("s"),
	PLUGIN_DIRECTOR("dir"), 
	PLUGIN_FLASH("fla"),
	PLUGIN_GEARS("gears"), 
	PLUGIN_JAVA("java"), 
	PLUGIN_PDF("pdf"), 
	PLUGIN_QUICKTIME("qt"),
	PLUGIN_REAL_PLAYER("realp"), 
	PLUGIN_SILVERLIGHT("ag"), 
	PLUGIN_WINDOWS_MEDIA("wma"),
	SUPPORT_COOKIES("cookie"),
	HEADER_USER_AGENT("ua"),
	HEADER_ACCEPT_LANGUAGE("lang"),
	USER_ID("uid"),
	VISITOR_CUSTOM_ID("cid"),
	NEW_VISIT("new_visit"),

	/** Optional Action info (measure Page view, Outlink, Download, Site search) */
	PAGE_CUSTOM_VARIABLE("cvar"),
	OUTLINK_URL("link"),
	DOWNLOAD_URL("download"),
	SEARCH_QUERY("search"),
	SEARCH_CATEGORY("search_cat"),
	SEARCH_RESULTS_COUNT("search_count"),
	ACTION_ID("pv_id"),
	GOAL_ID("idgoal"), 
	GOAL_REVENUE("revenue"),	
	ACTION_TIME_MILLIS("gt_ms"), 
	CHARACTER_SET("cs"),
	
	/** Optional Event Tracking info */
	EVENT_CATEGORY("e_c"),
	EVENT_ACTION("e_a"),	 
	EVENT_NAME("e_n"), 
	EVENT_VALUE("e_v"),
	
	/** Optional Content Tracking info */
	CONTENT_NAME("c_n"), 
	CONTENT_PIECE("c_p"), 
	CONTENT_TARGET("c_t"),
	CONTENT_INTERACTION("c_i"),	
	
	/** Optional Ecommerce info */
	ECOMMERCE_ID("ec_id"),
	ECOMMERCE_ITEMS("ec_items"),
	ECOMMERCE_REVENUE("revenue"),
	ECOMMERCE_SUBTOTAL("ec_st"), 
	ECOMMERCE_TAX("ec_tx"),
	ECOMMERCE_SHIPPING_COST("ec_sh"),
	ECOMMERCE_DISCOUNT("ec_dt"), 
	ECOMMERCE_LAST_ORDER_TIMESTAMP("_ects"),
	 
	/** Other parameters */
	VISITOR_IP("cip"),
	REQUEST_DATETIME("cdt"), 
	VISITOR_COUNTRY("country"),
	VISITOR_REGION("region"),
	VISITOR_CITY("city"),
	VISITOR_LATITUDE("lat"), 
	VISITOR_LONGITUDE("long"),
	TRACK_BOT_REQUESTS("bots"),
	  
	/** Media Analytics parameters */
	MEDIA_ID("ma_id"), 
	MEDIA_TITLE("ma_ti"), 
	MEDIA_RESOURCE_URL("ma_re"),  
	MEDIA_TYPE("ma_mt"), 
	MEDIA_PLAYER_NAME("ma_pn"), 
	MEDIA_PLAYTIME_SECONDS("ma_st"), 
	MEDIA_LENGTH_SECONDS("ma_le"), 
	MEDIA_PROGRESS("ma_ps"), 
	MEDIA_START_SECONDS("ma_ttp"), 
	MEDIA_RESOLUTION_WIDTH("ma_w"),
	MEDIA_RESOLUTION_HEIGHT("ma_h"),
	MEDIA_FULLSCREEN("ma_fs");

	private String value;

	private QueryParameter(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
