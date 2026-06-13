package crelia.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Crelia 1.21.1 自解压启动器(零依赖,照搬 26.1.2 的 crelia-launcher,启动形态换成 FML 4)。
 *
 * FML 4 走 BootstrapLauncher + 模块层:引导件上模块路径(-p),其余上 classpath,
 * 由 -DignoreList 防止游戏 jar 被错误模块化。嵌入库分为 boot / cp 两组(见索引文件)。
 *
 * ignoreList、vanilla 资源来源等启动参数都从 /META-INF/crelia-launch.properties 读取,
 * 该文件在构建时从 runServerFml 任务实时抽取,保证发布包与开发期运行环境逐字一致。
 */
public final class Main {
    private static final String INDEX_RESOURCE = "/META-INF/crelia-libraries.index";
    private static final String LAUNCH_PROPS_RESOURCE = "/META-INF/crelia-launch.properties";
    private static final String LIBRARY_RESOURCE_PREFIX = "/META-INF/crelia-libraries/";
    // 与 FML 启动脚本一致的 JVM 开关(模块化反射访问)
    private static final List<String> FIXED_JVM_ARGS = List.of(
        "--add-opens=java.base/java.util.jar=cpw.mods.securejarhandler",
        "--add-opens=java.base/java.lang.invoke=cpw.mods.securejarhandler",
        "--add-exports=java.base/sun.security.util=cpw.mods.securejarhandler",
        "--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming",
        "-Djava.net.preferIPv6Addresses=system",
        "-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=true",
        "-Dio.papermc.paper.suppress.sout.nags=true"
    );
    private static final List<String> SERVER_ARGS = List.of(
        "--launchTarget", "forgeserverdev",
        "--fml.neoForgeVersion", "21.1.77",
        "--fml.fmlVersion", "4.0.42",
        "--fml.mcVersion", "1.21.1",
        "--fml.neoFormVersion", "20240808.144430",
        "--nogui"
    );

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        // Crelia: 锚定到 jar 所在目录——双击运行时工作目录不可预测,所有文件(eula、库、世界)都放 jar 旁边
        Path baseDir = jarDirectory();
        System.out.println("Crelia base directory: " + baseDir);

        if (!hasAgreedToEula(baseDir.resolve("eula.txt"))) {
            return;
        }

        Properties launch = readLaunchProps();
        String ignoreList = launch.getProperty("ignoreList", "");
        String vanillaResourcesFrom = launch.getProperty("vanillaResourcesFrom", "");
        String vanillaResourcesDirName = launch.getProperty("vanillaResourcesDir", "vanilla-resources");

        Path outputDir = baseDir.resolve("crelia-libraries").toAbsolutePath().normalize();
        Files.createDirectories(outputDir);
        List<Entry> entries = readEntries();
        List<String> bootPath = new ArrayList<>();
        List<String> classpath = new ArrayList<>(entries.size());
        String vanillaSourceSha = null;

        System.out.println("Checking embedded Crelia libraries in " + outputDir);
        for (Entry entry : entries) {
            Path outputFile = outputDir.resolve(entry.name()).normalize();
            if (!outputFile.startsWith(outputDir)) {
                throw new IllegalStateException("Invalid embedded library path: " + entry.name());
            }
            if (!Files.isRegularFile(outputFile) || !entry.sha256().equalsIgnoreCase(sha256(outputFile))) {
                extract(entry, outputFile);
            }
            if ("boot".equals(entry.group())) {
                bootPath.add(outputFile.toString());
            } else {
                classpath.add(outputFile.toString());
            }
            if (entry.name().equals(vanillaResourcesFrom)) {
                vanillaSourceSha = entry.sha256();
            }
        }

        // Crelia: 把 vanilla 的 assets/data 从胖 jar 解压成目录,喂给 -Dcrelia.vanillaResources
        // (服务端 ServerPacksSource 据此挂载原版数据包;否则配方/战利品/标签全空,生成世界崩)
        Path vanillaDir = null;
        if (!vanillaResourcesFrom.isBlank()) {
            Path fatJar = outputDir.resolve(vanillaResourcesFrom);
            vanillaDir = outputDir.resolve(vanillaResourcesDirName);
            ensureVanillaResources(fatJar, vanillaDir, vanillaSourceSha);
        }

        List<String> command = new ArrayList<>();
        command.add(javaExecutable().toString());
        command.addAll(FIXED_JVM_ARGS);
        if (!ignoreList.isBlank()) {
            command.add("-DignoreList=" + ignoreList);
        }
        if (vanillaDir != null) {
            command.add("-Dcrelia.vanillaResources=" + vanillaDir);
        }
        String extraJvmArgs = System.getenv("CRELIA_JVM_ARGS"); // 例如 "-Xms4G -Xmx8G"
        if (extraJvmArgs != null && !extraJvmArgs.isBlank()) {
            command.addAll(Arrays.asList(extraJvmArgs.trim().split("\\s+")));
        }
        command.add("-p");
        command.add(String.join(File.pathSeparator, bootPath));
        command.add("--add-modules");
        command.add("ALL-MODULE-PATH");
        command.add("-cp");
        command.add(String.join(File.pathSeparator, classpath));
        command.add("cpw.mods.bootstraplauncher.BootstrapLauncher");
        command.addAll(SERVER_ARGS);
        command.addAll(Arrays.asList(args));

        System.out.println("Starting Crelia server");
        int exitCode = new ProcessBuilder(command)
            .directory(baseDir.toFile())
            .inheritIO()
            .start()
            .waitFor();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static Path jarDirectory() throws URISyntaxException {
        Path source = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path dir = Files.isRegularFile(source) ? source.getParent() : Path.of("").toAbsolutePath();
        return dir.toAbsolutePath().normalize();
    }

    private static Properties readLaunchProps() throws IOException {
        Properties props = new Properties();
        try (InputStream input = Main.class.getResourceAsStream(LAUNCH_PROPS_RESOURCE)) {
            if (input != null) {
                props.load(input);
            }
        }
        return props;
    }

    /** 解压胖 jar 里的 assets/ data/ 到目录;用一个 sha 标记文件避免每次重复解压。 */
    private static void ensureVanillaResources(Path fatJar, Path targetDir, String sourceSha) throws IOException {
        Path marker = targetDir.resolve(".crelia-vres-sha");
        if (Files.isDirectory(targetDir) && Files.isRegularFile(marker)
            && sourceSha != null && sourceSha.equalsIgnoreCase(Files.readString(marker).trim())) {
            return; // 已是最新,跳过
        }
        System.out.println("Extracting vanilla resources from " + fatJar.getFileName());
        if (Files.exists(targetDir)) {
            deleteRecursively(targetDir);
        }
        Files.createDirectories(targetDir);
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(fatJar))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || !(name.startsWith("assets/") || name.startsWith("data/"))) {
                    continue;
                }
                Path dest = targetDir.resolve(name).normalize();
                if (!dest.startsWith(targetDir)) {
                    throw new IllegalStateException("Bad zip entry: " + name);
                }
                Files.createDirectories(dest.getParent());
                Files.copy(zip, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        if (sourceSha != null) {
            Files.writeString(marker, sourceSha);
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static boolean hasAgreedToEula(Path eulaFile) throws IOException {
        Properties properties = new Properties();
        if (Files.isRegularFile(eulaFile)) {
            try (InputStream input = Files.newInputStream(eulaFile)) {
                properties.load(input);
            }
            if (Boolean.parseBoolean(properties.getProperty("eula", "false"))) {
                return true;
            }
        } else {
            properties.setProperty("eula", "false");
            try (OutputStream output = Files.newOutputStream(eulaFile)) {
                properties.store(output, "By changing the setting below to TRUE you are indicating your agreement to the Minecraft EULA (https://aka.ms/MinecraftEULA).");
            }
        }

        System.out.println("You need to agree to the Minecraft EULA before the server can start.");
        System.out.println("Edit eula.txt, set eula=true, then run this jar again.");
        System.out.println("首次运行已生成 eula.txt,请把其中 eula=false 改为 eula=true 后再次运行本 jar。");
        return false;
    }

    private static List<Entry> readEntries() throws IOException {
        try (InputStream input = Main.class.getResourceAsStream(INDEX_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing embedded library index: " + INDEX_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines()
                    .filter(line -> !line.isBlank())
                    .map(Entry::parse)
                    .toList();
            }
        }
    }

    private static void extract(Entry entry, Path outputFile) throws IOException {
        System.out.println("Unpacking " + entry.name());
        Files.createDirectories(outputFile.getParent());
        Path temporaryFile = outputFile.resolveSibling(outputFile.getFileName() + ".tmp");
        try (InputStream input = Main.class.getResourceAsStream(LIBRARY_RESOURCE_PREFIX + entry.name())) {
            if (input == null) {
                throw new IllegalStateException("Missing embedded library: " + entry.name());
            }
            Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
        }
        if (!entry.sha256().equalsIgnoreCase(sha256(temporaryFile))) {
            throw new IllegalStateException("Embedded library checksum mismatch: " + entry.name());
        }
        Files.move(temporaryFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String sha256(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
        try (InputStream input = Files.newInputStream(file)) {
            input.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), digest));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static Path javaExecutable() {
        String executableName = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path executable = Path.of(System.getProperty("java.home"), "bin", executableName);
        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException("Cannot find Java executable: " + executable);
        }
        return executable;
    }

    private record Entry(String sha256, String group, String name) {
        private static Entry parse(String line) {
            String[] fields = line.split("\t", 3);
            if (fields.length != 3 || fields[0].isBlank() || fields[1].isBlank() || fields[2].isBlank()) {
                throw new IllegalStateException("Malformed embedded library index line: " + line);
            }
            return new Entry(fields[0], fields[1], fields[2]);
        }
    }
}
