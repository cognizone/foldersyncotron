package cogni.zone.tools.foldersyncotron.cmdline;

import cogni.zone.tools.foldersyncotron.action.CreateFolderStatus;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import lombok.Getter;

import java.io.File;
import java.time.LocalDateTime;

@Parameters(commandDescription = "Create status of folder")
@Getter
public class CommandGetStatusFile extends FolderSyncotronCommand {

  private final LocalDateTime startTime = LocalDateTime.now();

  @Parameter(names = {"-c", "--compareMethod"}, description = "sizeAndDate: Faster but less accurate, crc32: Slower but more accurate, none: just check if the file exists")
  private CompareMethod compareMethod = CompareMethod.crc32;

  @Parameter(names = {"-i", "--inputfolder"}, required = true, description = "Input folder", validateValueWith = InputFolderValidator.class)
  private File inputFolder;

  @Parameter(names = {"-o", "--outputfile"}, required = true, description = "Output JSON file", validateValueWith = OutputFileValidator.class)
  private File outputFile;

  @Override
  public void run() {
    new CreateFolderStatus(this).run();
  }

  public static class InputFolderValidator implements IValueValidator<File> {
    @Override
    public void validate(String name, File inputFolder) {
      if (!inputFolder.isDirectory() || !inputFolder.canRead()) throw new ParameterException("InputFolder not a folder or not readable: " + inputFolder);
    }
  }

  public static class OutputFileValidator implements IValueValidator<File> {
    @Override
    public void validate(String name, File outputFile) {
      if (outputFile.isDirectory()) throw new ParameterException("OutputFile is a folder: " + outputFile);
      if (outputFile.exists() && !outputFile.isFile() && !outputFile.canWrite()) throw new ParameterException("OutputFile is not writable: " + outputFile);
    }
  }
}
