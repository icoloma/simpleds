package org.simpleds.schema;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A series of SchemaAction to process to make an schema upgrade.
 *  
 * @author icoloma
 *
 */
public class ActionRepository {

	/** repository of all SchemaAction to process */
	private Map<String, DatastoreAction> actions = Maps.newHashMap();
	
	/** list of root actions */
	private List<DatastoreAction> rootActions = Lists.newArrayList();
	
	private static Log log = LogFactory.getLog(ActionRepository.class);
	
	public ActionRepository add(DatastoreAction... actions) {
		for (DatastoreAction action : actions) {
			rootActions.add(action);
			this.actions.put(action.getPath(), action);
			for (DatastoreAction nestedAction : action.getActions()) {
				this.actions.put(nestedAction.getPath(), nestedAction);
			}
		}
		log.info("Registered the following actions: " + this.actions.keySet());
		return this;
	}
	
	public void proceed(HttpServletRequest request) {
		// extract the queueURL and the params
		Map<String, String> params = Maps.newHashMap();
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String name = e.nextElement();
			params.put(name, request.getParameter(name));
		}
		
		String queueUrl = request.getRequestURI();
		int pos = queueUrl.indexOf('?');
		if (pos != -1) {
			queueUrl = queueUrl.substring(0, pos);
		}
		
		// process it
		String actionId = params.get(ActionParamNames.ACTION);
		if (actionId == null || actionId.length() == 0) {
			for (DatastoreAction action : rootActions) {
				action.deferProceed(null, queueUrl, params);
			}
		} else {
			DatastoreAction action = actions.get(actionId);
			if (action == null) {
				throw new IllegalArgumentException("Could not find action: " + actionId);
			}
			proceed(action, queueUrl, params);
		}
	}

	private void proceed(DatastoreAction action, String queueUrl, Map<String, String> params) {
		String path = action.getPath();
		log.info("Entering schema action: " + path);
		log.info("Processed " + action.proceed(queueUrl, params) + " entities");
		log.info("Exiting schema migration action: " + path);
	}
	
}
