package com.six_group.java_ee.common.junit.client;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

class Archives {

    /**
     * Recursively deletes a directory. If the directory does not exist it's ignored.
     *
     * @param dir the directory
     *
     * @throws java.lang.IllegalArgumentException if the argument is not a directory
     */
    public static void deleteDirectory(final Path dir) throws IOException {
        if (Files.notExists(dir)) return;
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(String.format("Path '%s' is not a directory.", dir));
        }
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }

    /**
     * Unzips the zip file to the target directory.
     *
     * @param zipFile   the zip file to unzip
     * @param targetDir the directory to extract the zip file to
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    public static void unzip(final Path zipFile, final Path targetDir) throws IOException {
        final Path archive = getArchive(zipFile);

        try (final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(Files.newInputStream(archive)))) {
            ArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                final Path extractTarget = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTarget);
                } else {
                    Files.createDirectories(extractTarget.getParent());
                    Files.copy(in, extractTarget);
                }
            }
        } catch (ArchiveException e) {
            throw new IOException(e);
        }
    }

    private static Path getArchive(final Path path) throws IOException {
        final Path result;
        // Get the extension
        final String fileName = path.getFileName().toString();
        final String loweredFileName = fileName.toLowerCase(Locale.ENGLISH);
        if (loweredFileName.endsWith(".gz")) {
            String tempFileName = fileName.substring(0, loweredFileName.indexOf(".gz"));
            final int index = tempFileName.lastIndexOf('.');
            if (index > 0) {
                result = Files.createTempFile(tempFileName.substring(0, index), tempFileName.substring(index, tempFileName.length()));
            } else {
                result = Files.createTempFile(tempFileName.substring(0, index), "");
            }
            try (final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
                Files.copy(in, result, StandardCopyOption.REPLACE_EXISTING);
            } catch (CompressorException e) {
                throw new IOException(e);
            }
        } else {
            result = path;
        }
        return result;
    }
}
