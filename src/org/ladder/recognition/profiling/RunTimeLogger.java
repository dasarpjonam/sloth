package org.ladder.recognition.profiling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;

public class RunTimeLogger {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger.getLogger(RunTimeLogger.class);
	
	/**
	 * Run times for sections of code. Users of this class would add the runtime
	 * to the map using their defined keywords, and the map will be updated to
	 * keep track of each time the key is updated.
	 */
	private Map<String, List<Long>> m_runTimes = null;
	
	
	/**
	 * Default constructor. Initializes the class variables.
	 */
	public RunTimeLogger() {
		
		m_runTimes = new HashMap<String, List<Long>>();
	}
	

	/**
	 * Add a runtime for a given keyword. If the keyword has not been seen
	 * before, a new entry in the class run time map will be created. Otherwise,
	 * the time value will be appended to the current list of run times for that
	 * keyword.
	 * 
	 * @param key
	 *            the keyword being logged.
	 * @param runTime
	 *            run time of the section tied to the keyword.
	 */
	public void addRunTime(String key, long runTime) {
		
		List<Long> times = m_runTimes.get(key);
		
		if (times == null) {
			times = new ArrayList<Long>();
		}
		
		times.add(runTime);
		
		m_runTimes.put(key, times);
		
		log.info("\t" + times.size() + "\t" + key + "\t" + runTime);
	}
	

	/**
	 * Output the average run times to the log
	 */
	public void logAveragedResults() {
		
		for (String key : m_runTimes.keySet()) {
			List<Long> times = m_runTimes.get(key);
			long avgTime = 0;
			for (Long t : times) {
				avgTime += t;
			}
			avgTime /= times.size();
			
			log.info("\t" + "AVG" + "\t" + key + "\t" + avgTime);
		}
	}
}
