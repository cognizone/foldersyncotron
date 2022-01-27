package cogni.zone.tools.foldersyncotron.cmdline;

import cogni.zone.tools.foldersyncotron.action.CreateDiffData;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import lombok.Getter;

import java.io.File;
import java.time.LocalDateTime;

@Parameters(commandDescription = "Create difference data")
@Getter
public class CommandCreateDiffData extends FolderSyncotronCommand {

  private final LocalDateTime startTime = LocalDateTime.now();

  @Parameter(names = {"-i", "--inputfolder"}, required = true, description = "Input folder", validateValueWith = InputFolderValidator.class)
  private File inputFolder;

  @Parameter(names = {"-f", "--statusfile"}, required = true, description = "Input JSON file (status of \"other\" side)", validateValueWith = InputFileValidator.class)
  private File inputFile;

  @Parameter(names = {"-o", "--outputfolder"}, required = true, description = "Folder where to write result (needs to be empty)", validateValueWith = OutputFolderValidator.class)
  private File outputFolder;

  @Parameter(names = {"-z", "--createEmptyFiles"}, required = false, description = "Create empty files (no content, just create structure with existing files)")
  private boolean createEmptyFiles;

  @Override
  public void run() {
    new CreateDiffData(this).run();
  }

  public static class InputFolderValidator implements IValueValidator<File> {
    @Override
    public void validate(String name, File inputFolder) {
      if (!inputFolder.isDirectory() || !inputFolder.canRead()) throw new ParameterException("InputFolder not a folder or not readable: " + inputFolder);
    }
  }

  public static class InputFileValidator implements IValueValidator<File> {
    @Override
    public void validate(String name, File inputFile) {
      if (!inputFile.isFile() || !inputFile.canRead()) throw new ParameterException("InputFile not a file or not readable: " + inputFile);
    }
  }

  public static class OutputFolderValidator implements IValueValidator<File> {
    @Override
    public void validate(String name, File outputFolder) {
      if (outputFolder.isFile()) throw new ParameterException("OutputFolder is a file: " + outputFolder);
      if (outputFolder.exists() && !outputFolder.isDirectory() && !outputFolder.canWrite()) throw new ParameterException("OutputFolder is not writable: " + outputFolder);
    }
  }
}
