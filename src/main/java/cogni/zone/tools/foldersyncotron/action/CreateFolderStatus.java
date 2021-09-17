package cogni.zone.tools.foldersyncotron.action;

import cogni.zone.tools.foldersyncotron.cmdline.CommandGetStatusFile;
import cogni.zone.tools.foldersyncotron.tools.FileChecksumCreator;
import cogni.zone.tools.foldersyncotron.tools.FileHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "AssertStatement"})
@RequiredArgsConstructor
public class CreateFolderStatus {
  private final CommandGetStatusFile commandInfo;
  private final Map<String, String> checksumPerFile = new LinkedHashMap<>();
  private int fileCounter = 0;
  private int folderCounter = 0;

  public void run() {
    System.out.println("-----------------------------------------------");
    System.out.println("  Creating folder status file");
    System.out.println("     Running for folder " + commandInfo.getInputFolder());
    System.out.println("     Will write result in " + commandInfo.getOutputFile());
    System.out.println("     Checksum method: " + commandInfo.getCompareMethod());
    System.out.println("-----------------------------------------------");

    System.out.println("Running...");
    new FileHandler(this::handleFolder, this::handleFile).runFolder(commandInfo.getInputFolder());
    System.out.println(); //outputStats writes everything on 1 line
    storeResult();
    System.out.println("Thank you, come again!");
  }

  @SneakyThrows
  private void storeResult() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("startTime", commandInfo.getStartTime().toString());
    result.put("endTime", LocalDateTime.now().toString());
    result.put("compareMethod", commandInfo.getCompareMethod());
    result.put("inputFolder", commandInfo.getInputFolder());
    result.put("inputMachine", InetAddress.getLocalHost().getHostName());
    result.put("fileCount", fileCounter);
    result.put("folderCount", folderCounter);
    result.put("files", checksumPerFile);
    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(commandInfo.getOutputFile(), result);
  }

  private void handleFile(File file, String path) {
    if (checksumPerFile.containsKey(path)) throw new RuntimeException("???? Duplicate file: " + path);
    checksumPerFile.put(path, FileChecksumCreator.createChecksum(file, commandInfo.getCompareMethod()));
    fileCounter++;
    outputCurrentStats();
  }

  private void handleFolder(File folder, String path) {
    folderCounter++;
    outputCurrentStats();
  }

  private void outputCurrentStats() {
    System.out.print("\r   Processed " + fileCounter + " files & " + folderCounter + " folders...");
  }

}
