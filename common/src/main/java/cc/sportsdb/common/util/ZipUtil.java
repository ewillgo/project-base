package cc.sportsdb.common.util;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class ZipUtil {

    private ZipUtil() {

    }

    public static List<ZipFile> unzip(String filePath) throws IOException {
        return unzip(new File(filePath));
    }

    public static List<ZipFile> unzip(File zipFile) throws IOException {
        List<ZipFile> zipFileList = new ArrayList<>();

        if (zipFile == null || !zipFile.exists() || !zipFile.isFile()) {
            return zipFileList;
        }

        String ext = getFileExt(zipFile.getName());
        if (StringUtils.isEmpty(ext) || !".zip".equalsIgnoreCase(ext)) {
            return zipFileList;
        }

        int len;
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            ByteArrayOutputStream bos;
            while ((zipEntry = zis.getNextEntry()) != null) {
                bos = new ByteArrayOutputStream();
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                zipFileList.add(new ZipFile(zipEntry.getName(), bos));
            }
        }

        return zipFileList;
    }

    public static ZipPackage zip(MultipartFile[] files) throws IOException {
        return zip(files, null);
    }

    public static ZipPackage zip(MultipartFile[] files, ZipFileKeyGenerator generator) throws IOException {
        ZipFile[] zipFiles = new ZipFile[files.length];

        for (int i = 0; i < zipFiles.length; ++i) {
            MultipartFile file = files[i];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(file.getBytes());
            zipFiles[i] = new ZipFile(file.getOriginalFilename(), bos);
        }

        return zip(zipFiles, generator);
    }

    public static ZipPackage zip(ZipFile[] files) throws IOException {
        return zip(files, null);
    }

    public static ZipPackage zip(ZipFile[] files, ZipFileKeyGenerator generator) throws IOException {
        boolean isError = false;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(bos)) {
            for (ZipFile file : files) {
                String fileName = generator != null ? generator.getName() : file.getName();
                fileName = fileName.lastIndexOf('.') > 0 ? fileName : fileName + getFileExt(file.getName());
                zipOutputStream.putNextEntry(new ZipEntry(fileName));
                zipOutputStream.write(file.getOutputStream().toByteArray());
            }
        } catch (IOException e) {
            isError = true;
            throw e;
        } finally {
            if (isError) {
                bos.close();
            }
        }

        return new ZipPackage(bos);
    }

    private static boolean buildFileDir(File file) {
        return new File(file.getParent()).mkdirs();
    }

    private static String getFileExt(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }

        int pos;
        if ((pos = fileName.lastIndexOf('.')) < 0) {
            return null;
        }

        return fileName.substring(pos);
    }

    public interface ZipFileKeyGenerator {
        String getName();
    }

    public static class ZipPackage {
        private ByteArrayOutputStream zipOutputStream;

        public ZipPackage(ByteArrayOutputStream zipOutputStream) {
            this.zipOutputStream = zipOutputStream;
        }

        public ByteArrayOutputStream getOutputStream() {
            return zipOutputStream;
        }

        public void transferTo(File destFile) throws IOException {
            if (zipOutputStream != null) {
                buildFileDir(destFile);
                FileCopyUtils.copy(zipOutputStream.toByteArray(), destFile);
            }
        }
    }

    public static class ZipFile {
        private String name;
        private ByteArrayOutputStream outputStream;

        public ZipFile(String name, ByteArrayOutputStream outputStream) {
            this.name = name;
            this.outputStream = outputStream;
        }

        public void transferTo(File destFile) throws IOException {
            if (outputStream != null) {
                buildFileDir(destFile);
                FileCopyUtils.copy(outputStream.toByteArray(), destFile);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ByteArrayOutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public String toString() {
            return "ZipFile{" +
                    "name='" + name + '\'' +
                    ", outputStream=" + outputStream +
                    '}';
        }
    }
}
