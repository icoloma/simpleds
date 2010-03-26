package org.simpleds.schema;

import java.util.Enumeration;
import java.util.HashMap;
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
public class ActionLauncher {
	
	/** the default batch size to use */
	public static int DEFAULT_BATCH_SIZE = 150;
	
	/** repository of all SchemaAction to process */
	private Map<String, DatastoreAction> actions = Maps.newHashMap();
	
	/** list of root actions */
	private List<DatastoreAction> rootActions = Lists.newArrayList();
	
	private static Log log = LogFactory.getLog(ActionLauncher.class);
	
	public ActionLauncher add(DatastoreAction... actions) {
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
	
	/**
	 * Launch all configured actions in the configured uri
	 */
	public void launch(String uri) {
		for (DatastoreAction action : rootActions) {
			action.clearStatus();
			action.deferProceed(null, uri, new HashMap<String, String>());
		}
	}
	
	/**
	 * Launch the {@link DatastoreAction} instance specified by the "action" request param.
	 * @param request the current request object
	 */
	public void proceed(HttpServletRequest request) {
		// extract the queueURL and the params
		Map<String, String> params = Maps.newHashMap();
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String name = e.nextElement();
			params.put(name, request.getParameter(name));
		}
		
		String uri = request.getRequestURI();
		int pos = uri.indexOf('?');
		if (pos != -1) {
			uri = uri.substring(0, pos);
		}
		
		// process it
		String actionId = params.get(ActionParamNames.ACTION);
		if (actionId == null || actionId.length() == 0) {
			log.info("Action name is missing. Invoking launch() instead");
			launch(uri);
		} else {
			DatastoreAction action = actions.get(actionId);
			if (action == null) {
				throw new IllegalArgumentException("Could not find action: " + actionId);
			}
			proceed(action, uri, params);
		}
	}
	
	/**
	 * Return the status of the launched tasks.
	 */
	public TaskStats getStatus() {
		
	}
	
	/**
	 * Set the queue to be used by all configured actions. This call is ignored for actions configured 
	 * explicitely using {@link DatastoreAction}.withQueue()
	 * @param queueName the name of the queue to be used. 
	 * @return this instance for chaining
	 */
	public ActionLauncher withQueue(String queueName) {
		for (DatastoreAction action : actions.values()) {
			if (action.getQueueName() == null) { // set only if has not been set before
				action.withQueue(queueName);
			}
		}
		return this;
	}
	
	/**
	 * Set the queue to be used by all configured actions. This call is ignored for actions configured 
	 * explicitely using {@link DatastoreAction}.withBatchSize()
	 * @param batchSize the number of entities to be process by each invocation
	 * @return this instance for chaining
	 */
	public ActionLauncher withBatchSize(int batchSize) {
		for (DatastoreAction action : actions.values()) {
			if (action.getBatchSize() == null) { // set only if has not been set before
				action.withBatchSize(batchSize);
			}
		}
		return this;
	}

	private void proceed(DatastoreAction action, String queueUrl, Map<String, String> params) {
		String path = action.getPath();
		log.info("Entering schema action: " + path);
		log.info("Processed " + action.proceed(queueUrl, params) + " entities");
		log.info("Exiting schema migration action: " + path);
	}
	
}
