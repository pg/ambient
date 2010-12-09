/*
 * Created on Jun 4, 2003
 */
package edu.duke.cs.snarfer;

import java.io.File;

import edu.duke.cs.snarfer.PackageEntry;

/**
 * Objects implementing this interface participate in the process of fetching
 * packages by modulating the way the fetching process takes place and
 * displaying information about its progress.
 */
public interface FetchListener {
    /**
     * To be called before a new entry (file or archive) is to be fetched.
     * 
     * @param entry
     *            the package entry about to be fetched.
     * @param size
     *            the expected size of entry.
     */
    void onBeginEntry(PackageEntry entry, long size);

    /**
     * To be called before a new file is extracted from an archive package
     * entry.
     * 
     * @param archive
     *            the archive that contains file.
     * @param file
     *            the File being extracted from archive.
     * @param size
     *            the expected size of the file.
     */
    void onBeginArchivedFile(PackageEntry archive, File file, long size);

    /**
     * To be called throughout the process of fetching a package entry. This
     * method should be called after a call to onBeginEntry(...) and should
     * apply to the data read for that entry.
     * 
     * @see FetchListener#onBeginEntry(PackageEntry, long)
     * @param bytesRead
     *            the number of bytes that have been read/fetched since the last
     *            call to this method.
     */
    void onEntryProgress(int bytesRead);

    /**
     * To be called throughout the process of unpacking an archive package
     * entry. This method should be called after a call to
     * onBeginArchiveFile(...) and should apply to the data read from that
     * archive.
     * 
     * @see FetchListener#onBeginArchivedFile(PackageEntry, File, long)
     * @param bytesRead
     *            the number of bytes read since thhe last call to this method.
     */
    void onArchivedFileProgress(int bytesRead);

    /**
     * To be called when a file (either an individual entry or a file coming
     * from an archive) has the same name as a file that already exists on the
     * client's system. This function should specify whether the existing file
     * should be overwritten with the new file or not.
     * 
     * @param file
     *            the new file being fetched.
     * @return <code>true</code> if the old file is to be overwritten with the
     *         new one, <code>false</code> otherwise.
     */
    boolean onFileExists(File file);

    /**
     * To be called when an individual file (either a single file package entry
     * or a file coming from an archive package entry) has been successfully
     * installed on the client's system.
     * 
     * @param file
     *            the file that has been successfully installed.
     */
    void onFileFinished(File file);

    /**
     * To be called when an entire package entry (single file or archive) has
     * been successfully installed onto the user's system.
     * 
     * @param entry
     *            that package entry that has been isntalled.
     */
    void onEntryFinished(PackageEntry entry);
}
