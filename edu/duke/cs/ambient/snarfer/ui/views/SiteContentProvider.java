/*
 * Created on May 29, 2003
 */
package edu.duke.cs.ambient.snarfer.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.snarfer.PackageException;
import edu.duke.cs.snarfer.Package;
import edu.duke.cs.snarfer.PackageSite;

/**
 * This class manages the tree displayed in the Snarf Browser view.
 * 
 * @author jett
 */
public class SiteContentProvider implements ITreeContentProvider {
    class Category {
        private String name;

        private Map subs;

        private Set leaves;

        private Collection children;

        public Category(String name) {
            this.name = name;
            subs = new HashMap();
            leaves = new HashSet();
            children = new ArrayList();
        }

        public String getName() {
            return name;
        }

        public Set getSubNames() {
            return subs.keySet();
        }

        public Category getSub(String name) {
            return (Category) subs.get(name);
        }

        public Set getPackages() {
            return leaves;
        }

        public void addSub(Category child) {
            subs.put(child.getName(), child);
            children.add(child);
        }

        public void addPackage(Package pkg) {
            leaves.add(pkg);
            children.add(pkg);
        }

        public Object[] getChildren() {
            return children.toArray();
        }
    }

    private static Object[] EMPTY_ARRAY = new Object[0];

    private Collection urls;

    private Map roots;

    private Map parentMap;

    public SiteContentProvider() {
    }

    protected void build(IProgressMonitor monitor) {
        parentMap = new HashMap();
        roots = new HashMap();
        if (urls == null)
            return;
        monitor.beginTask("Contacting sites...", urls.size());
        Iterator it = urls.iterator();
        while (it.hasNext()) {
            URL url = (URL) it.next();
            monitor.subTask("Accessing " + url);
            PackageSite site = null;
            String errMsg = null;
            try {
                site = AmbientPlugin.getDefault().getSnarferSettings().getSite(
                        url);
            } catch (PackageException e) {
                errMsg = "Unable to process site index";
            }
            Category root = null;
            if (errMsg != null) {
                root = new Category("Error: " + errMsg + " (" + url + ")");
            } else {
                root = new Category(site.getName());
                int i, numPackages = site.getPackageCount();
                for (i = 0; i < numPackages; i++) {
                    Package pkg = site.getPackage(i);
                    add(pkg, root);
                }
            }
            roots.put(root, url);
            monitor.worked(1);
        }
    }

    private void add(Package pkg, Category root) {
        String cats = pkg.getCategory();
        Category c = root;
        Category sub = c.getSub(cats);
        if (sub == null) {
            sub = new Category(cats);
            c.addSub(sub);
            parentMap.put(sub, c);
        }
        c = sub;
        c.addPackage(pkg);
        parentMap.put(pkg, c);
    }

    public URL getSiteURL(Object item) {
        while (parentMap.containsKey(item))
            item = parentMap.get(item);
        if (roots.containsKey(item)) {
            return (URL) roots.get(item);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Category) {
            return ((Category) parentElement).getChildren();
        }
        return EMPTY_ARRAY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        return parentMap.get(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        return (element instanceof Category)
                && (((Category) element).getPackages().size() > 0 || ((Category) element)
                        .getSubNames().size() > 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (urls == null)
            return EMPTY_ARRAY;
        return roots.keySet().toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        urls = (Collection) newInput;
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                build(monitor);
            }
        };
        try {
            new ProgressMonitorDialog(viewer.getControl().getShell()).run(true,
                    true, op);
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
    }

}
