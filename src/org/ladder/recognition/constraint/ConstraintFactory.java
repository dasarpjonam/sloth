/**
 * ConstraintFactory.java
 * 
 * Revision History:<br>
 * Aug 22, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;

/**
 * Constructs instances of constraints based on the given name of the constraint
 * that you want to create. Turns string names into instances, like reflection.
 * 
 * @author jbjohns
 */
public class ConstraintFactory {
	
	/**
	 * Logger
	 */
	private static Logger log = LadderLogger.getLogger(ConstraintFactory.class);
	
	/**
	 * Package we're pulling constraints from
	 */
	protected static final String CONSTRAINT_PACKAGE = "org.ladder.recognition.constraint.confidence";
	
	/**
	 * Any prefix that the creator of the constraints put in the java file names
	 */
	protected static final String CONSTRAINT_CLASS_PREFIX = "";
	
	/**
	 * Any suffix that the creator of the constraints pu in the java file names
	 */
	protected static final String CONSTRAINT_CLASS_SUFFIX = "Constraint";
	
	/**
	 * Cache the class for each constraint. This gives us a bit of a speedup
	 * instead of performing Class.forName() each time.
	 */
	private static Map<String, Class<?>> S_CONSTRAINT_CLASS_MAP = Collections
	        .synchronizedMap(new HashMap<String, Class<?>>());
	
	/**
	 * Cache IConstraint objects, which you can do .newInstance() on to get new
	 * isntances of constraints by name.
	 */
	private static Map<String, IConstraint> S_CONSTRAINT_OBJECT_MAP = Collections
	        .synchronizedMap(new HashMap<String, IConstraint>());
	
	
	/**
	 * Get an instance of the constraint with the given name. If the constraint
	 * does not exist, will return null.
	 * <p>
	 * Classes are loaded through reflection, using {@link Class#newInstance()}.
	 * The correct class is loaded with {@link Class#forName(String)}. The name
	 * of the class that's looked for is built by appending:
	 * <p>
	 * {@link #CONSTRAINT_PACKAGE}+{@link #CONSTRAINT_CLASS_PREFIX}+<b>name</b>+
	 * {@link #CONSTRAINT_CLASS_SUFFIX}
	 * 
	 * @see #getConstraint(String, boolean) with flag to use suffix set to
	 *      <code>true</code>
	 * @param name
	 *            The name of the constraint to construct
	 * @return The constraint with the given name, or null if it does not exist
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static IConstraint getConstraint(String name)
	        throws InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		
		return getConstraint(name, true);
	}
	

	/**
	 * Get an instance of the constraint with the given name. If the constraint
	 * does not exist, will return null.
	 * <p>
	 * Classes are loaded through reflection, using {@link Class#newInstance()}.
	 * The correct class is loaded with {@link Class#forName(String)}. The name
	 * of the class that's looked for is built by appending:
	 * <p>
	 * {@link #CONSTRAINT_PACKAGE}+{@link #CONSTRAINT_CLASS_PREFIX}+<b>name</b>+
	 * {@link #CONSTRAINT_CLASS_SUFFIX}
	 * 
	 * @param name
	 *            The name of the constraint to construct
	 * @param suffix
	 *            Do we use the {@link #CONSTRAINT_CLASS_SUFFIX} on the end of
	 *            the name of the constraint?
	 * @return The constraint with the given name, or null if it does not exist
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static IConstraint getConstraint(String name, boolean suffix)
	        throws InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		
		log.debug("Constraint name : " + name);
		
		// do we already have an instance of this constraint?
		IConstraint constraint = S_CONSTRAINT_OBJECT_MAP.get(name);
		
		// if not, load the instance through reflection
		if (constraint == null) {
			// class name of the constraint with this name
			String className = CONSTRAINT_PACKAGE + "."
			                   + CONSTRAINT_CLASS_PREFIX + name;
			if (suffix) {
				className += CONSTRAINT_CLASS_SUFFIX;
			}
			log.debug("Class name: " + className);
			
			// get the constraint through reflection
			constraint = constraintForClassName(className);
			// put the instance into the object map so we don't have to reflect
			// next time
			S_CONSTRAINT_OBJECT_MAP.put(name, constraint);
		}
		
		// get a new instance of the constraint with this name
		constraint = constraint.newInstance();
		
		return constraint;
	}
	

	/**
	 * Get the constraint object for the given constraint definition. This
	 * method is similar to {@link #getConstraint(String)} except it also sets
	 * the constraint's threshold based on any threshold multiplier set in the
	 * constraint definition. If the constraint definition does not correspond
	 * to a real constraint (no matching name) this method will return null.
	 * 
	 * @param constraintDefinition
	 *            The constraint definition to get the constraint object for (by
	 *            name, see {@link #getConstraint(String)})
	 * @return The constraint object for this constraint definition, with the
	 *         threshold set according to the constraint definition's threshold
	 *         multiplier. Null if the constraint does not exist.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static IConstraint getConstraint(
	        ConstraintDefinition constraintDefinition)
	        throws InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		IConstraint constraint = getConstraint(constraintDefinition.getName(),
		        true);
		if (constraint != null) {
			constraint.multiplyThreshold(constraintDefinition
			        .getThresholdMultiplier());
		}
		return constraint;
	}
	

	/**
	 * Get the constraint (reflectively) for the class with the given class name
	 * 
	 * @param className
	 *            The name of the class to load reflectively -- fully qualified
	 * @return The constraint with the given name
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	private static IConstraint constraintForClassName(String className)
	        throws InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		IConstraint constraint = null;
		
		Class<?> constraintClass = S_CONSTRAINT_CLASS_MAP.get(className);
		if (constraintClass == null) {
			constraintClass = Class.forName(className);
			S_CONSTRAINT_CLASS_MAP.put(className, constraintClass);
		}
		
		if (constraintClass == null) {
			throw new ClassNotFoundException("Class " + className
			                                 + " not loaded");
		}
		log.debug("Class: " + constraintClass);
		constraint = (IConstraint) constraintClass.newInstance();
		log.debug("Constraint : " + constraint.getName());
		
		return constraint;
	}
	

	/**
	 * Get all the names of the classes of constraints that are declared in the
	 * package. This method instantiates all the classes and caches the
	 * Constraint name ({@link IConstraint#getName()}) with the
	 * {@link Class#newInstance()} result.
	 * 
	 * @return A list of all the class names for the constraints being used in
	 *         the system. Constraints are found under the
	 *         {@link #CONSTRAINT_PACKAGE} package.
	 * @throws ClassNotFoundException
	 */
	public static Set<String> loadAllConstraintNames()
	        throws ClassNotFoundException {
		// list of constraint class objects
		List<Class<?>> constraintClasses = null;
		
		constraintClasses = getClassesForPackage(CONSTRAINT_PACKAGE);
		
		for (Class<?> c : constraintClasses) {
			// don't want Interfaces or Abstract classes
			int classModifiers = c.getModifiers();
			if (!Modifier.isInterface(classModifiers)
			    && !Modifier.isAbstract(classModifiers)) {
				
				try {
					// instantiate this class
					IConstraint constraint = (IConstraint) c.newInstance();
					// put in the map
					S_CONSTRAINT_CLASS_MAP.put(constraint.getName(), c);
				}
				catch (InstantiationException e) {
					log.error("Could not instantiate a constraint");
					log.error(e);
				}
				catch (IllegalAccessException e) {
					log.error("Illegal access");
					log.error(e);
				}
				catch (ClassCastException cce) {
					log.error(c.getName()
					          + " cannot be cast to IConstraint, ignoring");
				}
			}
		}
		
		return getAllCachedConstraintNames();
	}
	

	/**
	 * Get a copy of the set of all the names of the constraints that have been
	 * loaded into the Factory's cache. This will return a non-null set, but may
	 * be empty if no classes have been loaded.
	 * 
	 * @return The set of all names that have been cached
	 */
	public static Set<String> getAllCachedConstraintNames() {
		Set<String> constraintNames = new TreeSet<String>();
		constraintNames.addAll(S_CONSTRAINT_CLASS_MAP.keySet());
		
		return constraintNames;
	}
	

	/**
	 * Attempts to list all the classes in the specified package as determined
	 * by the context class loader.<br>
	 * <br>
	 * Credit for the bulk of this method goes to <a href=
	 * "http://forums.sun.com/emailmessage!default.jspa?messageID=4169249">this
	 * post</a>.
	 * 
	 * @param pckgname
	 *            the package name to search
	 * @return a list of classes that exist within that package
	 * @throws ClassNotFoundException
	 *             if something went wrong
	 */
	private static List<Class<?>> getClassesForPackage(String pckgname)
	        throws ClassNotFoundException {
		
		// This will hold a list of directories matching the pckgname. There may
		// be more than one if a package is split over multiple jars/paths
		List<File> directories = new ArrayList<File>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			String path = pckgname.replace('.', '/');
			
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(path);
			while (resources.hasMoreElements()) {
				directories.add(new File(URLDecoder.decode(resources
				        .nextElement().getPath(), "UTF-8")));
			}
		}
		catch (NullPointerException x) {
			throw new ClassNotFoundException(
			        pckgname
			                + " does not appear to be a valid package (Null pointer exception)");
		}
		catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(
			        pckgname
			                + " does not appear to be a valid package (Unsupported encoding)");
		}
		catch (IOException ioex) {
			throw new ClassNotFoundException(
			        "IOException was thrown when trying to get all resources for "
			                + pckgname);
		}
		
		List<Class<?>> classes = new ArrayList<Class<?>>();
		
		// For every directory identified capture all the .class files
		for (File directory : directories) {
			
			if (directory.exists()) {
				
				// Get the list of the files contained in the package
				String[] files = directory.list();
				if (directory.list() == null)
					continue;
				for (String file : files) {
					// We are only interested in .class files
					if (file.endsWith(".class")) {
						// Removes the .class extension
						classes.add(Class.forName(pckgname
						                          + '.'
						                          + file.substring(0, file
						                                  .length() - 6)));
					}
					// Recursively get all packages in the class
					else {
						classes.addAll(getClassesForPackage(pckgname + "."
						                                    + file.toString()));
					}
				}
			}
			else {
				throw new ClassNotFoundException(
				        pckgname + " (" + directory.getPath()
				                + ") does not appear to be a valid package");
			}
		}
		
		return classes;
	}
}
