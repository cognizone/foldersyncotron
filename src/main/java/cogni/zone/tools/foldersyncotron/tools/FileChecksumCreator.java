package cogni.zone.tools.foldersyncotron.tools;

import cogni.zone.tools.foldersyncotron.cmdline.CompareMethod;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class FileChecksumCreator {

  public static String createChecksum(File file, CompareMethod compareMethod) {
    if (compareMethod == CompareMethod.crc32) return crc32(file);
    if (compareMethod == CompareMethod.sizeAndDate) return sizeAndDate(file);
    if (compareMethod == CompareMethod.size) return size(file);
    if (compareMethod == CompareMethod.date) return date(file);
    if (compareMethod == CompareMethod.none) return "none";
    throw new RuntimeException("Unknown CompareMethod " + compareMethod);
  }

  @SneakyThrows
  private static String crc32(File file) {
    try (FileInputStream fis = new FileInputStream(file)) {
      CheckedInputStream checkedInputStream = new CheckedInputStream(fis, new CRC32());
      IOUtils.copy(checkedInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
      return "crc32:" + checkedInputStream.getChecksum().getValue();
    }
  }

  private static String sizeAndDate(File file) {
    return "sizeAndDate:" + file.length() + "_" + (file.lastModified() / 1000L); //divide by 1000 because we might lose the milliseconds on the way...
  }

  private static String size(File file) {
    return "size:" + file.length();
  }

  private static String date(File file) {
    return "date:" + (file.lastModified() / 1000L); //divide by 1000 because we might lose the milliseconds on the way...
  }

}
