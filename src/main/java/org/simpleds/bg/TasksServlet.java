package org.simpleds.bg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

/**
 * Launches a {@link BackgroundTask}.
 * This servlet must be configured in web.xml:
 * <pre>
	&lt;servlet>
		&lt;servlet-name>tasks&lt;/servlet-name>
		&lt;servlet-class>org.simpleds.bg.TasksServlet&lt;/servlet-class>
	&lt;/servlet>
	&lt;servlet-mapping>
		&lt;servlet-name>tasks&lt;/servlet-name>
		&lt;url-pattern>/tasks&lt;/url-pattern>
	&lt;/servlet-mapping>

	&lt;security-constraint>
		&lt;web-resource-collection>
			&lt;url-pattern>/tasks&lt;/url-pattern>
		&lt;/web-resource-collection>
		&lt;auth-constraint>
			&lt;role-name>admin&lt;/role-name>
		&lt;/auth-constraint>
	&lt;/security-constraint>
 * </pre>
 * 
 * It accepts two methods:
 * <ul>
 * <li>POST: this is the method used by AppEngine Queues. A <code>task</code> parameter 
 * is required, which specifies the task path that will be invoked. 
 * </li>
 * <li>GET: When invoked with GET, this servlet will display the stats of the configured tasks.
 * </li>
 * </ul>
 * 
 * Tasks are retrieved by searching for a {@link TaskLauncher} instance stored as <code>_task-launcher</code> 
 * in the application context. If missing, an exception will be thrown.
 *  
 * @author icoloma
 *
 */
public class TasksServlet extends HttpServlet {
	
	/** 960gs reset+fonts CSS slightly modified */
	private static String STYLESHEET = "<style type=\"text/css\">" +
	"html,body,div,span,applet,object,iframe,h1,h2,h3,h4,h5,h6,p,blockquote,pre,a,abbr,acronym,address,big,cite,code,del,dfn,em,font,img,ins,kbd,q,s,samp,small,strike,strong,sub,sup,tt,var,b,u,i,center,dl,dt,dd,ol,ul,li,fieldset,form,label,legend,table,caption,tbody,tfoot,thead,tr,th,td{margin:0;padding:0;border:0;outline:0;font-size:100%;vertical-align:baseline;background:transparent}body{line-height:1}ol,ul{list-style:none}blockquote,q{quotes:none}blockquote:before,blockquote:after,q:before,q:after{content:'';content:none}:focus{outline:0}ins{text-decoration:none}del{text-decoration:line-through}table{border-collapse:collapse;border-spacing:0}" +
	"body{font:13px/1.5 'Helvetica Neue',Arial,'Liberation Sans',FreeSans,sans-serif}a:focus{outline:1px dotted invert}hr{border:0 #ccc solid;border-top-width:1px;clear:both;height:0}h1{font-size:25px}h2{font-size:23px}h3{font-size:21px}h4{font-size:19px}h5{font-size:17px}h6{font-size:15px}ol{list-style:decimal}ul{list-style:none}li{margin-left:30px}p,dl,hr,h1,h2,h3,h4,h5,h6,ol,ul,pre,table,address,fieldset{margin-bottom:10px}" +
	"</style>\n"
	;

	/** the id of the task to execute, leave empty for all */
	public static final String TASK_PARAM = "task";
	
	/** the {@link TaskLauncher} attribute to use */
	private static TaskLauncher taskLauncher;
	
	private static Log log = LogFactory.getLog(TasksServlet.class);
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		TaskLauncher launcher = getTaskLauncher();
		
		// extract the queueURL and the params
		Map<String, String> params = Maps.newHashMap();
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String name = e.nextElement();
			params.put(name, request.getParameter(name));
		}
		
		String uri = request.getRequestURI();
		
		// process it
		String taskId = params.get(TASK_PARAM);
		if (taskId == null || taskId.length() == 0) {
			throw new IllegalArgumentException("Missing " + TASK_PARAM + " attribute. Cannot proceed.");
		} else {
			long results = launcher.launch(uri, taskId, params);
			PrintWriter writer = response.getWriter();
			writer.print("<html><head><title>Task launched successfully</title>" + STYLESHEET + "</head><body>");
			writer.print("Task successfully launched. Processed " + results + " results in this batch, check the stats to get more info.");
			writer.print("</body></html>");
		}
	}

	private TaskLauncher getTaskLauncher() {
		if (taskLauncher == null) {
			throw new RuntimeException("Missing TaskLauncher instance, please register one using TasksServlet.setTaskLauncher()");
		}
		return taskLauncher;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		writer.print("<html><head><title>Task stats</title>" + STYLESHEET + "</head><body>");
		writer.print("<h1>Task stats</h1>");
		writer.print("<ul>");
		for (TaskStats stats: getTaskLauncher().getStats()) {
			writer.print("<li>");
			writer.print("<strong>" + stats.getPath() + ":</strong>");
	        writer.print("<ul>");
	        writer.print("<li>start: " + stats.getStart() + "</li>");
	        writer.print("<li>end: " + stats.getEnd() + "</li>");
	        writer.print("<li>executionCount: " + stats.getExecutionCount() + "</li>");
	        writer.print("<li>entitiesCount: " + stats.getEntityCount() + "</li>");
	        writer.print("</ul>");
			writer.print("</li>");
		}
		writer.print("</ul>");
		writer.print("</body></html>");
	}

	public static void setTaskLauncher(TaskLauncher taskLauncher) {
		TasksServlet.taskLauncher = taskLauncher;
	}

}
