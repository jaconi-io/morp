package io.jaconi.morp;

import org.testcontainers.utility.DockerImageName;

public class ArmUtil {

    public static DockerImageName select(String armImage, String defaultImage) {
        return isARM64()
                ? DockerImageName.parse(armImage).asCompatibleSubstituteFor(defaultImage)
                : DockerImageName.parse(defaultImage);
    }

    public static boolean isARM64() {
        return System.getProperty("os.arch").equals("aarch64");
    }
}
