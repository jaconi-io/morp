package io.jaconi.morp;

import org.testcontainers.containers.DefaultRecordingFileFactory;
import org.testcontainers.containers.VncRecordingContainer;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class TruncatedRecordingFileFactory extends DefaultRecordingFileFactory {

    @Override
    public File recordingFileForTest(File vncRecordingDirectory, String prefix, boolean succeeded) {
        return super.recordingFileForTest(vncRecordingDirectory, transformPrefix(prefix), succeeded);
    }

    @Override
    public File recordingFileForTest(File vncRecordingDirectory, String prefix, boolean succeeded, VncRecordingContainer.VncRecordingFormat recordingFormat) {
        return super.recordingFileForTest(vncRecordingDirectory, transformPrefix(prefix), succeeded, recordingFormat);
    }

    private String transformPrefix(String prefix) {
        var decodedPrefix = URLDecoder.decode(prefix, StandardCharsets.UTF_8);

        decodedPrefix = substringAfter(decodedPrefix,"/");
        decodedPrefix = substringBefore(decodedPrefix, "(") + substringAfter(decodedPrefix, ")");

        return URLEncoder.encode(decodedPrefix, StandardCharsets.UTF_8);
    }
}
