package cc.sportsdb.common.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public abstract class UploadUtil {

    private UploadUtil() {

    }

    public static void uploadFile(MultipartFile file, MultipartFileHandler handler) throws IOException {
        uploadFile(new MultipartFile[]{file}, handler);
    }

    public static void uploadFile(MultipartFile[] files, MultipartFileHandler handler) throws IOException {

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files to handle.");
        }

        for (MultipartFile file : files) {
            if (handler != null) {
                handler.handle(new MultipartFileHandler.EhanceMultipartFile(file));
            }
        }

    }

    public interface MultipartFileHandler {
        void handle(EhanceMultipartFile ehanceMultipartFile) throws IOException;

        class EhanceMultipartFile {

            private boolean image;
            private MultipartFile file;
            private Thumbnails.Builder<BufferedImage> imageBuilder;
            private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";
            private static final String IMAGE_OPERATION_TIPS = "Image %s operation need a image file.";
            private static final double DEFAULT_QUALITY = 1.0D;

            public EhanceMultipartFile(MultipartFile file) {
                this.file = file;
                setImage(file.getContentType().startsWith(IMAGE_CONTENT_TYPE_PREFIX));
            }

            public String getFileName() {
                return file.getName();
            }

            public String getOriginalFilename() {
                return file.getOriginalFilename();
            }

            public long getFileSize() {
                return file.getSize();
            }

            public String getFileExt() {
                int pos;
                String fileName = file.getOriginalFilename();

                if ((pos = fileName.lastIndexOf('.')) < 0) {
                    return null;
                }

                return file.getOriginalFilename().substring(pos + 1);
            }

            public boolean isImage() {
                return image;
            }

            private void setImage(boolean image) {
                this.image = image;
            }

            public EhanceMultipartFile crop(int width, int height) throws IOException {
                crop(width, height, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile crop(int width, int height, double quality) throws IOException {
                assertImage("[crop]");
                buildBufferedImage();
                imageBuilder.outputQuality(quality).size(width, height).crop(Positions.CENTER);
                return this;
            }

            public EhanceMultipartFile resize(int width, int height) throws IOException {
                resize(width, height, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile resize(int width, int height, double quality) throws IOException {
                assertImage("[resize]");
                buildBufferedImage();
                imageBuilder.outputQuality(quality).size(width, height);
                return this;
            }

            public EhanceMultipartFile rotate() {

                assertImage("[rotate]");

                return this;
            }

            public EhanceMultipartFile scale(double width, double height) throws IOException {
                scale(width, height, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile scale(double width, double height, double quality) throws IOException {
                assertImage("[scale]");
                buildBufferedImage();
                imageBuilder.outputQuality(quality).scale(width, height);
                return this;
            }

            public EhanceMultipartFile waterMark() {
                assertImage("[watermark]");

                return this;
            }

            public ByteArrayOutputStream getOutputStream() throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                if (imageBuilder != null) {
                    imageBuilder.toOutputStream(bos);
                } else {
                    bos.write(file.getBytes());
                }
                return bos;
            }

            public void transferTo(File destFile) throws IOException {
                buildFileDir(destFile);
                if (imageBuilder != null) {
                    imageBuilder.toFile(destFile);
                } else {
                    file.transferTo(destFile);
                }
            }

            public void buildFileDir(File file) {
                File dir = new File(file.getPath().substring(
                        0, file.getPath().lastIndexOf(File.separator)));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }

            private void buildBufferedImage() throws IOException {
                if (!isImage() || imageBuilder != null) {
                    return;
                }

                imageBuilder = Thumbnails.of(ImageIO.read(file.getInputStream()));
            }

            private void assertImage(String operationName) {
                if (!isImage()) {
                    throw new IllegalArgumentException(String.format(IMAGE_OPERATION_TIPS, operationName));
                }
            }
        }
    }
}
