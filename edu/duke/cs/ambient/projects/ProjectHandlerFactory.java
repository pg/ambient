/*
 * Created on Jun 28, 2005
 */
package edu.duke.cs.ambient.projects;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

/**
 * This class provides information about exisiting project types and their
 * specific handlers. This information is based on both the project types
 * defined in the Ambient plug-in as well as any extesions to the
 * edu.duke.cs.ambient.projectTypes extension point.
 * <p>
 * Use {@link ProjectHandlerFactory#getInstance()}to obtain an instance of this
 * class.
 * 
 * @see ProjectHandlerFactory#getInstance()
 * @author Marcin Dobosz
 * @since 2.0
 */
public class ProjectHandlerFactory {

    // Some relevent constants.
    private static final String EXTENSION_POINT_ID = "edu.duke.cs.ambient.projectTypes";

    private static final String ID_ATTRIBUTE = "id";

    private static final String NAME_ATTRIBUTE = "name";

    private static final String CLASS_ATTRIBUTE = "class";

    /**
     * The singleton instance.
     */
    private static ProjectHandlerFactory myInstance = null;

    /**
     * Mapping from typeIds (Strings) to IConfigurationElements that are to be
     * used to create the appropriate class instances.
     */
    private Map myHandlerMap;

    /**
     * All valid UI-friendly names of registered handlers
     */
    private String[] names;

    /**
     * All registered handler types.
     */
    private String[] types;

    /**
     * This is a convenience container class for storing the 3 relevent pieces
     * of information about registered handlers.
     */
    private class ProjectType {
        private String myName;

        private String myTypeId;

        private IConfigurationElement myConfig;

        public ProjectType(String name, String typeId,
                IConfigurationElement element) {
            myName = name;
            myTypeId = typeId;
            myConfig = element;
        }

        public IConfigurationElement getConfig() {
            return myConfig;
        }

        public String getName() {
            return myName;
        }

        public String getTypeId() {
            return myTypeId;
        }
    }

    /**
     * This exception indicates that a specified project type is not a valid
     * project type id.
     */
    public class InvalidProjectTypeException extends Exception {
        private static final long serialVersionUID = 2042509273392105453L;

        /**
         * Creates a new InvalidProjectTypeException with no message.
         */
        public InvalidProjectTypeException() {
            super();
        }

        /**
         * Creates a new InvalidProjectTypeException with a message.
         * 
         * @param message
         *            the message of this exception.
         */
        public InvalidProjectTypeException(String message) {
            super(message);
        }
    }

    /**
     * Creates a new ProjectFactory. Hidden from public to conform to singleton
     * pattern.
     */
    private ProjectHandlerFactory() {
        myHandlerMap = new TreeMap();
        readRegistry();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "";
    }

    /**
     * Reads the extension registry and retrieves information about all the
     * extension points that contribute project handlers.
     */
    private void readRegistry() {
        IExtension[] extensions = Platform.getExtensionRegistry()
                .getExtensionPoint(EXTENSION_POINT_ID).getExtensions();

        ArrayList myTypes = new ArrayList();
        for (int i = 0; i < extensions.length; i++) {
            IExtension currentExtension = extensions[i];
            IConfigurationElement[] configElements = currentExtension
                    .getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                IConfigurationElement current = configElements[j];
                myTypes.add(new ProjectType(current
                        .getAttribute(NAME_ATTRIBUTE), current
                        .getAttribute(ID_ATTRIBUTE), current));
            }
        }
        prepareData(myTypes);
    }

    /**
     * Populates the myHandlerMap as well as the names and types arrays with the
     * relevent information.
     * 
     * @param typesArray
     *            an arraylist of ProjectType objects
     */
    private void prepareData(ArrayList typesArray) {
        int size = typesArray.size();
        types = new String[size];
        names = new String[size];

        for (int i = 0; i < size; i++) {
            ProjectType type = (ProjectType) typesArray.get(i);
            types[i] = type.getTypeId();
            names[i] = type.getName();
            myHandlerMap.put(types[i], type.getConfig());
        }
    }

    /**
     * Returns the singleton instance of ProjectFactory.
     * 
     * @return the singleton instance of ProjectFactory.
     */
    public static ProjectHandlerFactory getInstance() {
        if (myInstance == null)
            myInstance = new ProjectHandlerFactory();
        return myInstance;
    }

    /**
     * Returns an array of all the valid type ids available. Entries in the
     * resulting array correspond to the entries from
     * {@link ProjectHandlerFactory#getValidNames()}.
     * 
     * @return an array of all the valid type ids available.
     */
    public String[] getValidTypes() {
        return types;
    }

    /**
     * Returns an array of all the valid ui-frineldy project names available.
     * Entries in the resulting array correspond to the entries from
     * {@link ProjectHandlerFactory#getValidTypes()}.
     * 
     * @return an array of all the valid ui-frineldy project names available.
     */
    public String[] getValidNames() {
        return names;
    }

    /**
     * Returns <code>true</code> if the type specified by typeId is a valid
     * type id. The validation process is case-sensitive.
     * 
     * @param typeId
     *            a potential type id.
     * @return <code>true</code> if the specified type id is valid,
     *         <code>false</code> otherwise.
     */
    public boolean isValidType(String typeId) {
        return myHandlerMap.containsKey(typeId);
    }

    // TODO maybe some expcetion should be thrown here.

    /**
     * Returns a new instance of an IProjectHandler object responsible for
     * handling the type of project specified by typeId. If typeId is not valid,
     * a {@link ProjectHandlerFactory.InvalidProjectTypeException}is thrown.
     * 
     * @see ProjectHandlerFactory#isValidType(String)
     * @exception ProjectHandlerFactory.InvalidProjectTypeException
     *                thrown if typeId is not a valid type id.
     * @param typeId
     *            the type id for the desired project handler.
     * @return a new instance of an IProjectHandler object responsible for
     *         handling the type of project specified by typeId.
     */
    public IProjectHandler getHandler(String typeId)
            throws InvalidProjectTypeException {
        if (!isValidType(typeId))
            throw new InvalidProjectTypeException("The type \"" + typeId
                    + "\" is not a recongnized project type identifier");

        IProjectHandler result = null;
        IConfigurationElement className = (IConfigurationElement) myHandlerMap
                .get(typeId);
        // create appropriate class, if it can't be created result will
        // remain null
        try {
            result = (IProjectHandler) className
                    .createExecutableExtension(CLASS_ATTRIBUTE);
        } catch (CoreException e) {
        }
        return result;
    }
}
