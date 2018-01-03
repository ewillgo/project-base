package cc.sportsdb.common.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
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

    public static void handleFile(MultipartFile file, MultipartFileHandler handler) {
        handleFile(new MultipartFile[]{file}, handler);
    }

    public static void handleFile(MultipartFile[] files, MultipartFileHandler handler) {

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

        void handle(EhanceMultipartFile file);

        class EhanceMultipartFile {

            private boolean image;
            private MultipartFile file;
            private BufferedImage bufferedImage;
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

            public EhanceMultipartFile quality(double quality) throws IOException {
                assertImage("[quality]");
                buildBufferedImage();
                imageBuilder.useOriginalFormat().outputQuality(quality);
                return this;
            }

            public EhanceMultipartFile crop(int width, int height, double quality) throws IOException {
                crop(width, height, Positions.CENTER, quality);
                return this;
            }

            public EhanceMultipartFile crop(int width, int height) throws IOException {
                crop(width, height, Positions.CENTER);
                return this;
            }

            public EhanceMultipartFile crop(int width, int height, Position position) throws IOException {
                crop(width, height, position, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile crop(int width, int height, Position position, double quality) throws IOException {
                assertImage("[crop]");
                buildBufferedImage();
                imageBuilder
                        .scale(1.0)
                        .outputQuality(quality)
                        .useOriginalFormat()
                        .sourceRegion(position, Math.min(width, bufferedImage.getWidth()), Math.min(height, bufferedImage.getHeight()));
                return this;
            }

            public EhanceMultipartFile resize(int width, int height) throws IOException {
                resize(width, height, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile resize(int width, int height, double quality) throws IOException {
                assertImage("[resize]");
                buildBufferedImage();
                imageBuilder
                        .outputQuality(quality)
                        .useOriginalFormat()
                        .size(Math.min(width, bufferedImage.getWidth()), Math.min(height, bufferedImage.getHeight()))
                        .keepAspectRatio(false);
                return this;
            }

            public EhanceMultipartFile rotate(double angle) throws IOException {
                rotate(angle, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile rotate(double angle, double quality) throws IOException {
                assertImage("[rotate]");
                buildBufferedImage();
                imageBuilder
                        .size(bufferedImage.getWidth(), bufferedImage.getHeight())
                        .outputQuality(quality)
                        .keepAspectRatio(true)
                        .useOriginalFormat()
                        .rotate(angle);
                return this;
            }

            public EhanceMultipartFile scale(double rate) throws IOException {
                scale(rate, rate, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile scale(double width, double height) throws IOException {
                scale(width, height, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile scale(double width, double height, double quality) throws IOException {
                assertImage("[scale]");
                buildBufferedImage();
                imageBuilder.scale(width, height).outputQuality(quality).useOriginalFormat();
                return this;
            }

            public EhanceMultipartFile watermark(Position position, File watermarkFile, float opacity) throws IOException {
                watermark(position, watermarkFile, opacity, DEFAULT_QUALITY);
                return this;
            }

            public EhanceMultipartFile watermark(Position position, File watermarkFile, float opacity, double quality) throws IOException {
                assertImage("[watermark]");
                buildBufferedImage();
                imageBuilder
                        .size(bufferedImage.getWidth(), bufferedImage.getHeight())
                        .keepAspectRatio(true)
                        .outputQuality(quality)
                        .useOriginalFormat()
                        .watermark(position, ImageIO.read(watermarkFile), opacity);
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
                mkdirs(destFile);
                if (imageBuilder != null) {
                    imageBuilder.toFile(destFile);
                } else {
                    file.transferTo(destFile);
                }
            }

            private boolean mkdirs(File file) {
                return new File(file.getParent()).mkdirs();
            }

            private void buildBufferedImage() throws IOException {
                if (!isImage() || imageBuilder != null) {
                    return;
                }
                bufferedImage = ImageIO.read(file.getInputStream());
                imageBuilder = Thumbnails.of(bufferedImage);
            }

            private void assertImage(String operationName) {
                if (!isImage()) {
                    throw new IllegalArgumentException(String.format(IMAGE_OPERATION_TIPS, operationName));
                }
            }
        }
    }
}
