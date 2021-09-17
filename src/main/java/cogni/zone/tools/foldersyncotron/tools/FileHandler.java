package cogni.zone.tools.foldersyncotron.tools;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.function.BiConsumer;

@SuppressWarnings("AssertStatement")
@RequiredArgsConstructor
public class FileHandler {
  private final BiConsumer<File, String> folderHandler;
  private final BiConsumer<File, String> fileHandler;

  public void runFolder(File folder) {
    runFolder(folder, "");
  }

  private void runFolder(File folder, String path) {
    File[] files = folder.listFiles((FileFilter) FileFileFilter.FILE);
    assert null != files;
    for (File file : files) {
      fileHandler.accept(file, path + file.getName());
    }

    File[] directories = folder.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
    assert null != directories;
    Arrays.stream(directories).forEach(directory -> runFolder(directory, path + directory.getName() + "/"));
    folderHandler.accept(folder, path);
  }
}
