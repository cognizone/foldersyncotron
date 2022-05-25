package cogni.zone.tools.foldersyncotron.action;

import cogni.zone.tools.foldersyncotron.cmdline.CommandCreateDiffData;
import cogni.zone.tools.foldersyncotron.cmdline.CompareMethod;
import cogni.zone.tools.foldersyncotron.tools.FileChecksumCreator;
import cogni.zone.tools.foldersyncotron.tools.FileHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "AssertStatement"})
public class CreateDiffData {
  private static final long maxZipContentSize = 3L * 1024L * 1024L * 1024L; //3GB

  private final CommandCreateDiffData commandInfo;
  private final CompareMethod compareMethod;
  private final Map<String, String> checksumPerFile;

  private int checkedFiles = 0;
  private int addedFiles = 0;

  private FileOutputStream zipFileOutputStream;
  private ZipOutputStream zipOutputStream;
  private long totalZipSize = 0L;
  private long currentZipSize = 0L;
  private int lastZipFileIndex = 0;

  @SuppressWarnings("unchecked")
  @SneakyThrows
  public CreateDiffData(CommandCreateDiffData commandInfo) {
    this.commandInfo = commandInfo;

    Map<String, Object> values = (Map<String, Object>) new ObjectMapper().readValue(commandInfo.getInputFile(), Map.class);
    compareMethod = CompareMethod.valueOf((String) values.get("compareMethod"));
    checksumPerFile = (Map<String, String>) values.get("files");
  }

  public void run() {
    commandInfo.getOutputFolder().mkdirs();
    if (!commandInfo.getOutputFolder().isDirectory() || !commandInfo.getOutputFolder().canWrite() || Objects.requireNonNull(commandInfo.getOutputFolder().listFiles()).length > 0) {
      System.out.println("Cannot create or cannot write to outputFolder, or folder not empty: " + commandInfo.getOutputFolder());
      return;
    }

    System.out.println("-----------------------------------------------");
    System.out.println("  Creating difference data:");
    System.out.println("     InputFolder " + commandInfo.getInputFolder());
    System.out.println("     JSON \"other side\" inputFile " + commandInfo.getInputFile());
    System.out.println("     Will write result in " + commandInfo.getOutputFolder());
    System.out.println("     Checksum method: " + compareMethod);
    System.out.println("-----------------------------------------------");

    System.out.println("Running...");
    new FileHandler(this::handleFolder, this::handleFile).runFolder(commandInfo.getInputFolder());
    System.out.println(); //outputStats writes everything on 1 line
    closeCurrentZip();
    storeFilesToDelete();
    System.out.println("Thank you, come again!");
  }

  private void handleFile(File file, String path) {
    String otherSideChecksum = checksumPerFile.remove(path);
    checkedFiles++;
    if (null == otherSideChecksum || !otherSideChecksum.equals(FileChecksumCreator.createChecksum(file, compareMethod))) {
      addedFiles++;
      storeFile(file, path);
    }
    outputCurrentStats();
  }

  private void handleFolder(File folder, String path) {
  }

  @SneakyThrows
  private void storeFilesToDelete() {
    System.out.println("Not existing files to delete: " + checksumPerFile.size());
    PrintWriter printWriter = new PrintWriter(new File(commandInfo.getOutputFolder(), "notExisting_toRemove.txt"), StandardCharsets.UTF_8);
    checksumPerFile.keySet().forEach(path -> printWriter.println("rm " + path));
    printWriter.println();
    printWriter.flush();
    printWriter.close();
    if (printWriter.checkError()) System.out.println("Output to notExisting_toRemove.txt said error...");
  }

  @SneakyThrows
  private void storeFile(File file, String path) {
    if (null == zipOutputStream) {
      lastZipFileIndex++;
      zipFileOutputStream = new FileOutputStream(new File(commandInfo.getOutputFolder(), "data-" + lastZipFileIndex + ".zip"));
      zipOutputStream = new ZipOutputStream(zipFileOutputStream, StandardCharsets.UTF_8);
      zipOutputStream.setLevel(9);
      currentZipSize = 0;
    }

    ZipEntry entry = new ZipEntry(path);
    BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
    FileTime fileTime = FileTime.fromMillis(basicFileAttributes.lastModifiedTime().toMillis());
    entry.setCreationTime(fileTime);
    entry.setLastModifiedTime(fileTime);
    zipOutputStream.putNextEntry(entry);
    CloseShieldOutputStream closeShieldOutputStream = new CloseShieldOutputStream(zipOutputStream);
    if(commandInfo.isCreateEmptyFiles()) {
      IOUtils.write(path, closeShieldOutputStream, StandardCharsets.UTF_8);
    }
    else {
      FileUtils.copyFile(file, closeShieldOutputStream);
    }
    closeShieldOutputStream.flush();
    zipOutputStream.flush();
    zipOutputStream.closeEntry();

    currentZipSize += basicFileAttributes.size();
    totalZipSize += basicFileAttributes.size();

    if (currentZipSize >= maxZipContentSize && !commandInfo.isCreateEmptyFiles()) closeCurrentZip();
  }

  @SuppressWarnings("AssignmentToNull")
  @SneakyThrows
  private void closeCurrentZip() {
    if (null == zipOutputStream) return;

    zipOutputStream.flush();
    zipFileOutputStream.flush();
    zipOutputStream.close();
    zipFileOutputStream.close();

    zipOutputStream = null;
    zipFileOutputStream = null;
  }

  private void outputCurrentStats() {
    long mb = totalZipSize / 1024 / 1024;
    System.out.print("\r   Processed " + checkedFiles + " files - added " + addedFiles + " files - total bytes " + mb + "MB - " + lastZipFileIndex + " zip files...");
  }

}
