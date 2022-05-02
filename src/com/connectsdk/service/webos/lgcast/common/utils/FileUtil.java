package com.connectsdk.service.webos.lgcast.common.utils;

import android.content.Context;
import android.os.StatFs;
import android.webkit.MimeTypeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Locale;

public class FileUtil {
    public static ArrayList<String> getChildFileNames(final String path) {
        try {
            File baseFolder = new File(path);
            ArrayList<String> fileList = new ArrayList<>();

            if (baseFolder == null || baseFolder.exists() == false)
                throw new Exception("Invalid path");

            for (String file : baseFolder.list())
                if (FileUtil.isFile(baseFolder.toString() + "/" + file) == true)
                    fileList.add(file);

            return fileList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }
    }

    public static ArrayList<String> listAll(final File file) {
        try {
            if (file == null || file.exists() == false)
                throw new Exception("Invalid file");

            ArrayList<String> list = new ArrayList<String>();
            list.clear();

            if (file.isDirectory() == true)
                list.addAll(listFolder(file));
            else
                list.add(file.getPath());

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }
    }

    public static ArrayList<String> listFolder(final File folder) {
        try {
            if (folder == null || folder.exists() == false || folder.isDirectory() == false)
                throw new Exception("Invalid folder");

            ArrayList<String> list = new ArrayList<String>();
            list.clear();

            for (File childFile : folder.listFiles()) {
                if (childFile.isDirectory() == true)
                    list.addAll(listFolder(childFile));
                else
                    list.add(childFile.getPath());
            }

            list.add(folder.getPath());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }
    }

    public static int deleteAll(final String path) {
        try {
            File file = new File(path);

            if (file.isDirectory() == true)
                return deleteFolder(file);
            else
                return deleteFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int deleteFolder(final File folder) {
        try {
            int deletedCount = 0;

            if (folder == null || folder.exists() == false || folder.isDirectory() == false)
                throw new Exception("Invalid folder");

            for (File childFile : folder.listFiles()) {
                if (childFile.isDirectory() == true)
                    deletedCount += deleteFolder(childFile);
                else
                    deletedCount += deleteFile(childFile);
            }

            deletedCount += deleteFile(folder);
            return deletedCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int deleteFile(final File file) {
        try {
            if (file == null || file.exists() == false)
                throw new Exception("Invalid file");

            return (file.delete() == true) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isFile(final String path) {
        try {
            return (path != null) && new File(path).isFile();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDirectory(final String path) {
        try {
            return (path != null) && new File(path).isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean exists(final String path) {
        return exists((path != null) ? new File(path) : null);
    }

    public static boolean exists(final File file) {
        try {
            return (file != null) && file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static long size(final String path) {
        try {
            return (path != null) ? new File(path).length() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String read(String file) {
        try {
            return (file != null) ? read(new FileReader(file)) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String read(InputStream input) {
        return (input != null) ? read(new InputStreamReader(input)) : null;
    }

    public static String read(Reader reader) {
        try {
            if (reader == null)
                throw new NullPointerException("Invalid reader");

            StringBuffer buffer = new StringBuffer();
            BufferedReader bufferedReader = new BufferedReader(reader);

            while (true) {
                char[] read = new char[10 * 1024];
                int len = bufferedReader.read(read, 0, 10 * 1024);

                if (len <= 0)
                    break;

                buffer.append(read, 0, len);
            }

            bufferedReader.close();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readAsset(Context context, String assetFile) {
        try {
            return FileUtil.read(context.getAssets().open(assetFile));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean write(String file, String message) {
        try {
            return write(new FileWriter(file), message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean write(OutputStream output, String message) {
        return write(new OutputStreamWriter(output), message);
    }

    public static boolean write(Writer writer, String message) {
        try {
            if (writer == null)
                throw new NullPointerException("Null writer");

            PrintWriter pw = new PrintWriter(writer);
            pw.print(message);
            pw.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getPath(final String fileName) {
        int index = (fileName != null) ? fileName.lastIndexOf("/") : -1;
        return (index > -1) ? fileName.substring(0, index) : fileName;
    }

    public static String getLastPath(final String path) {
        if (path == null)
            return null;

        int index = path.lastIndexOf("/") + 1;
        return path.substring(index);
    }

    public static String getFileName(final String path) {
        return getLastPath(path);
    }

    public static String getExtension(final String path) {
        int index = (path != null) ? path.lastIndexOf(".") : -1;
        return (index > -1) ? path.substring(index + 1).toLowerCase(Locale.US) : null;
    }

    public static String getMimeType(final String path) {
        String extension = getExtension(path);
        return (extension != null) ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;
    }

    public static long getAvailableSpace(final String path) {
        long size = 0;

        if (path != null) {
            StatFs stat = new StatFs(path);
            stat.restat(path);
            size = stat.getAvailableBlocksLong() * stat.getAvailableBytes();
        }

        return size;
    }

    public static boolean makeFolders(final String folder) {
        if (exists(folder) == true)
            return true;

        File newFolder = (folder != null) ? new File(folder) : null;
        return (newFolder != null) && newFolder.mkdirs();
    }

    public static String getUniqueFolder(final String parentFolder, final String folderName) {
        try {
            if (parentFolder == null || folderName == null)
                throw new Exception();

            int count = 0;
            String postfix = "";

            while (true) {
                String uniquePath = parentFolder + "/" + folderName + postfix;

                if (FileUtil.exists(uniquePath) == false)
                    return uniquePath;

                count++;
                postfix = "(" + count + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUniqueFile(final String baseFolder, final String fileName) {
        try {
            if (baseFolder == null || fileName == null)
                throw new Exception();

            int pos = fileName.lastIndexOf('.');
            String name = (pos != -1) ? fileName.substring(0, pos) : fileName;
            String ext = (pos != -1) ? fileName.substring(pos + 1) : null;

            if (name == null || ext == null)
                throw new Exception();

            int count = 0;
            String midfix = "";

            while (true) {
                String uniqueName = baseFolder + "/" + name + midfix;

                if (ext != null)
                    uniqueName += "." + ext;

                if (FileUtil.exists(uniqueName) == false)
                    return uniqueName;

                count++;
                midfix = "(" + count + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUniqueFile(final String filePath) {
        return getUniqueFile(FileUtil.getPath(filePath), FileUtil.getFileName(filePath));
    }
}
