package cogni.zone.tools.foldersyncotron.cmdline;

import com.beust.jcommander.JCommander;

public class JCommanderBuilder {

  public static JCommander build() {
    return JCommander.newBuilder()
            .addCommand("getStatusFile", new CommandGetStatusFile())
            .addCommand("createDiffData", new CommandCreateDiffData())
            .build();
  }
}
