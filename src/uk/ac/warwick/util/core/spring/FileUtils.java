package uk.ac.warwick.util.core.spring;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.core.StringUtils;

/**
 * Singleton utility class.
 * 
 * @author xusqac
 */
public final class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class);

    private FileUtils() {
        // private empty constructor
    }

    /**
     * By default, deletes are strict and throw ISEs when bad things happen.
     */
    public static void recursiveDelete(final File file) {
        recursiveDelete(file, true, null);
    }
    
    public static void recursiveDelete(final File file, final boolean strict) {
        recursiveDelete(file, strict, null);
    }
    
    /**
     * Do a strict delete, but if the deletion fails then rename the directory to a recycle bin.
     */
    public static void recursiveDelete(final File file, final File deletionBin) {
        recursiveDelete(file, true, deletionBin);
    }

    /**
     * Delete the tree from the specified file. The boolean strict is a
     * direction to the method on what to do when the delete fails (this
     * sometimes happens when a file handle is open, on NFS it will rename
     * rather than move the file to some safe tmp directory, so you can't delete
     * the parent directory).
     */
    public static void recursiveDelete(final File file, final boolean strict, final File deletionBin) {
        if (file.isDirectory()) {
            if (LOGGER.isDebugEnabled()) {
                String logMessage = "Directory contains files (pre-delete):";
                logMessage += FileUtils.recursiveOutput(file);
                LOGGER.debug(logMessage);
            }
            
            for (File child: file.listFiles()) {
                recursiveDelete(child, strict, deletionBin);
            }
        }
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Deleting " + file);
        boolean deletedFileOK = file.delete();
        if (!deletedFileOK) {
            if (strict) {
                // on NFS we want to move the file to some safe temporary directory
                // and mark it to be deleted on exit
                
                LOGGER.error("Could not delete the " + (file.isDirectory() ? "directory" : "file") + " " + file);
                
                if (LOGGER.isDebugEnabled() && file.isDirectory()) {
                    if (file.list().length == 0) {
                        LOGGER.debug("Directory contains no files anymore though");
                    } else {
                        String logMessage = "Directory contains files:";
                        logMessage += FileUtils.recursiveOutput(file);
                        LOGGER.debug(logMessage);
                    }
                }
                
                if (deletionBin != null && file.isDirectory()) {
                    // rename the file to the deletion bin
                    if (!deletionBin.isDirectory() || !deletionBin.exists()) {
                        throw new IllegalArgumentException("Deletion bin " + deletionBin + " must be an existing directory");
                    } else {
                        File renameToFile = new File(deletionBin, file.getName() + System.currentTimeMillis());
                        
                        if (renameToFile.exists()) {
                            throw new IllegalStateException("Could not rename directory to " + renameToFile + " - file already exists");
                        } else {
                            boolean success = file.renameTo(renameToFile);
                            
                            if (!success) {
                                throw new IllegalStateException("Failed to rename directory to " + renameToFile);
                            }
                            
                            renameToFile.deleteOnExit();
                        }
                    }
                } else {
                    throw new IllegalStateException("Cannot delete " + file);
                }
            } else {
                file.deleteOnExit();
                LOGGER.error("Could not delete the file " + file + ", marked to delete on exit");
            }
        }
    }

    /**
     * Copies the specified source file (which must exist) into the target
     * (which must not exist). If the source is a file, then the contents of
     * that file will be copied into target. If the source is a directory, then
     * the contents of that directory will be copied into target, which will of
     * course be a directory. If recurse is true, then the directory will be
     * recursively copied.
     * 
     * @param source
     *            Source File or Directory.
     * @param target
     *            The target.
     * @param recurse
     *            Whether to recursively copy all directories.
     * @throws IOException
     */
    public static void copy(final File source, final File target, final boolean recurse) throws IOException {
        sanityCheck(source, target, recurse);

        if (source.isDirectory()) {
            File[] filesInDir = source.listFiles(); // *must* be taken before
            // target is created
            // in case the target is in the source directory
            if (!target.mkdirs() && !target.exists()) {
                throw new IOException("Cannot create " + target);
            }
            for (File child: filesInDir) {
                if (child.isFile() || recurse) {
                    copy(child, new File(target, child.getName()), recurse);
                }
            }
        } else {
            if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
                throw new IOException("Cannot create " + target);
            }
            FileCopyUtils.copy(source, target);
        }
    }

    private static void sanityCheck(final File source, final File target, final boolean recurse) {
        if (!source.exists()) {
            throw new IllegalArgumentException("The source " + source + " must exist.");
        }
        if (target.exists() && !target.isDirectory()) {
            throw new IllegalArgumentException("The target " + target + " must not exist.");
        }

        // if target doesn't exist .isFile() and .isDirectory() fail.
        if (target.exists() && source.isDirectory() != target.isDirectory()) {
            throw new IllegalArgumentException(String.format(
                    "Source '%s' and target '%s' must both be directories or both be files", source, target));
        }

        // must append a trailing slash else /calendar and /calendar-copy will
        // match
        if (recurse && target.getAbsolutePath().startsWith(source.getAbsolutePath() + "/")) {
            throw new IllegalArgumentException(String.format("Target '%s' cannot be a descendant of source '%s'", target, source));
        }
    }

    /**
     * Return just the fileName. If url is "/services/its/a.b" then return
     * "a.b".
     */
    public static String getFileName(final String url) {
        File file = new File(url);
        return file.getName();
    }

    /**
     * Return everything upto the last . if there is one.
     * 
     * @param s
     * @return
     */
    public static String getFileNameWithoutExtension(final String s) {
        int indexOfLastDot = StringUtils.safeSubstring(s, 1).lastIndexOf('.');
        if (indexOfLastDot < 0) {
            return s;
        }
        return s.substring(0, indexOfLastDot + 1);
    }

    /**
     * Return the extension of the specified fileName. If there is no extension,
     * this will return "".
     * @deprecated use getLowerCaseExtension, which does exactly what it says on the tin
     * 
     */
    public static String getExtension(final String s) {
        int indexOfLastDot = StringUtils.safeSubstring(s, 1).lastIndexOf('.');
        if (indexOfLastDot < 0) {
            return "";
        }
        return s.substring(indexOfLastDot + 2);
    }
    
    public static String getLowerCaseExtension(final String filename){
    	return getExtension(filename).toLowerCase();
    }
    
    public static boolean extensionMatches(final String filename, final String extension){
    	String compareExtension = extension.toLowerCase();
    	// if the user has specified an extension like ".txt", clean it up for them
    	compareExtension = compareExtension.replaceAll("[^\\.]*\\.", "");
    	return getLowerCaseExtension(filename).equalsIgnoreCase(compareExtension);
    }

    /**
     * Strip all invalid characters from the specified spring.
     * 
     * The safety refers to the fact that only ANSI characters remain so
     * there is no need to worry about escaping, and no slashes
     * are allowed to avoid any problems with path handling.
     * 
     * It removes a leading dot from the filename.
     */
    public static String convertToSafeFileName(final String originalName) {
        if (!StringUtils.hasText(originalName)) {
            return originalName;
        }

        // strip the path
        File file = new File(originalName.toLowerCase());
        String s = file.getName();
        String fileName = FileUtils.getFileNameWithoutExtension(s);
        String extension = FileUtils.getExtension(s);
        StringBuffer fileNameSB = new StringBuffer();
        for (byte b: StringUtils.create(fileName)) {
            char c = (char) b;
            if (isValidForFileName(c)) {
                fileNameSB.append(c);
            }
        }

        // Shouldn't start with dots
        while (fileNameSB.length() > 0 && fileNameSB.charAt(0) == '.') {
            fileNameSB.deleteCharAt(0);
        }

        if (StringUtils.hasText(extension)) {
            fileNameSB.append(".");
            for (byte b: StringUtils.create(extension)) {
                char c = (char) b;
                if (isValidForFileName(c)) {
                    fileNameSB.append(c);
                }
            }
        }

        // replace all whitespace with "_"
        String safeFileName = fileNameSB.toString();
        safeFileName = safeFileName.replaceAll(" ", "_");
        return safeFileName;
    }

    private static boolean isValidForFileName(final char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == ' '
                || c == '.';
    }

    /**
     * Create, or retrieve the specified file. Will create the file if it
     * doesn't already exist. Will convert any IOExceptions to runtime
     * exceptions.
     */
    public static File createOrLoadFile(final File parent, final String fileName) {
        if (!parent.isDirectory()) {
            throw new IllegalStateException("Parent " + parent + " must be a directory");
        }

        File file = new File(parent, fileName);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IllegalStateException("Cannot create file " + file);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create file " + file);
            }
        }
        return file;
    }
    
    /**
     * returns the directory structure of the file system as a string
     * 
     * @param file
     * @return
     */
    public static String recursiveOutput(final File file) {
        if (!file.exists()) {
            return "";
        }
        
        String output = "\n" + file.getPath();
        
        if (file.isDirectory()) {
            
            File[] subfiles = file.listFiles();
            for (File subfile : subfiles) {
                output += recursiveOutput(subfile);
            }
        } else {
            output += " ";
            output += file.length();
        }
        return output;
    }
}
